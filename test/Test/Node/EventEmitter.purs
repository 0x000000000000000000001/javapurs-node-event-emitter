module Test.Node.EventEmitter where

import Prelude

import Data.Either (Either(..))
import Data.Foldable (for_)
import Data.Tuple.Nested ((/\))
import Effect.Class (liftEffect)
import Effect.Exception as Exception
import Effect.Ref as Ref
import Effect.Uncurried (EffectFn1, EffectFn2, EffectFn3, mkEffectFn1, mkEffectFn2, runEffectFn1, runEffectFn2, runEffectFn3)
import Node.EventEmitter (EventEmitter, EventHandle(..), on, on_, once, once_, prependListener, prependListener_, prependOnceListener, prependOnceListener_, unsafeEmitFn)
import Node.EventEmitter as EventEmitter
import Node.EventEmitter.UtilTypes (EventHandle1, EventHandle2)
import Test.Spec (Spec, describe, it)
import Test.Spec.Assertions (fail, shouldEqual)

fooH :: EventHandle1 EventEmitter String
fooH = EventHandle "foo" mkEffectFn1

pairH :: EventHandle2 EventEmitter String String
pairH = EventHandle "pair" mkEffectFn2

spec :: Spec Unit
spec = describe "event-emitter" do
  it "`new` does not throw" do
    liftEffect $ void $ EventEmitter.new
  it "`emit` does not throw" do
    liftEffect do
      ee <- EventEmitter.new
      void $ runEffectFn1 (unsafeEmitFn ee :: EffectFn1 String Boolean) "foo"
      void $ runEffectFn2 (unsafeEmitFn ee :: EffectFn2 String String Boolean) "foo" "baz"
      void $ runEffectFn1 (unsafeEmitFn ee) "foo"
      void $ runEffectFn2 (unsafeEmitFn ee) "foo" "bar"
      void $ runEffectFn3 (unsafeEmitFn ee) "foo" "bar" "baz"
  it "`emit` reports whether a listener is registered" do
    liftEffect do
      ee <- EventEmitter.new
      absent <- runEffectFn2 (unsafeEmitFn ee :: EffectFn2 String String Boolean) "foo" "bar"
      absent `shouldEqual` false
      ee # on_ fooH \_ -> pure unit
      present <- runEffectFn2 (unsafeEmitFn ee :: EffectFn2 String String Boolean) "foo" "bar"
      present `shouldEqual` true
  it "`emit` forwards every argument" do
    liftEffect do
      first <- Ref.new ""
      second <- Ref.new ""
      ee <- EventEmitter.new
      ee # on_ pairH \left right -> do
        Ref.write left first
        Ref.write right second
      void $ runEffectFn3 (unsafeEmitFn ee :: EffectFn3 String String String Boolean) "pair" "left" "right"
      leftValue <- Ref.read first
      rightValue <- Ref.read second
      leftValue `shouldEqual` "left"
      rightValue `shouldEqual` "right"
  it "late listeners do not receive past events" do
    liftEffect do
      value <- Ref.new ""
      ee <- EventEmitter.new
      void $ runEffectFn2 (unsafeEmitFn ee) "foo" "early"
      ee # on_ fooH \received -> Ref.write received value
      current <- Ref.read value
      current `shouldEqual` ""
  it "`once` listeners run at most once" do
    liftEffect do
      calls <- Ref.new 0
      ee <- EventEmitter.new
      ee # once_ fooH \_ -> Ref.modify_ (_ + 1) calls
      void $ runEffectFn2 (unsafeEmitFn ee) "foo" "first"
      void $ runEffectFn2 (unsafeEmitFn ee) "foo" "second"
      total <- Ref.read calls
      total `shouldEqual` 1
  it "listener exceptions propagate from `emit`" do
    liftEffect do
      ee <- EventEmitter.new
      ee # on_ fooH \_ -> Exception.throwException (Exception.error "listener failure")
      result <- Exception.try $ runEffectFn2 (unsafeEmitFn ee :: EffectFn2 String String Boolean) "foo" "bar"
      case result of
        Left error -> Exception.message error `shouldEqual` "listener failure"
        Right _ -> fail "Expected the listener exception to propagate"
  describe "standard functions" do
    let
      fns =
        [ "on_" /\ on_
        , "once_" /\ once_
        , "prependListener_" /\ prependListener_
        , "prependOnceListener_" /\ prependOnceListener_
        ]
    for_ fns \(fnName /\ fn) -> do
      it (fnName <> " works") do
        liftEffect do
          let expected = "bar"
          ref <- Ref.new ""
          ee <- EventEmitter.new
          ee # fn fooH \val -> do
            Ref.write val ref
          void $ runEffectFn2 (unsafeEmitFn ee) "foo" expected
          val <- Ref.read ref
          val `shouldEqual` expected

  describe "subscribe functions" do
    let
      fns =
        [ "onSubscribe" /\ on
        , "onceSubscribe" /\ once
        , "prependListenerSubscribe" /\ prependListener
        , "prependOnceListenerSubscribe" /\ prependOnceListener
        ]
    for_ fns \(fnName /\ fn) -> do
      it (fnName <> " - normal call works") do
        liftEffect do
          let expected = "bar"
          ref <- Ref.new ""
          ee <- EventEmitter.new
          void $ ee # fn fooH \val -> do
            Ref.write val ref
          void $ runEffectFn2 (unsafeEmitFn ee) "foo" expected
          val <- Ref.read ref
          val `shouldEqual` expected
    for_ fns \(fnName /\ fn) -> do
      it (fnName <> " - unsubscribing before call works") do
        liftEffect do
          ref <- Ref.new ""
          ee <- EventEmitter.new
          remove <- ee # fn fooH \val -> do
            Ref.write val ref
          remove
          void $ runEffectFn2 (unsafeEmitFn ee) "foo" "bar"
          val <- Ref.read ref
          val `shouldEqual` ""
