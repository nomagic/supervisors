package com.nomagicsoftware.supervisor;

import com.nomagicsoftware.event.DeathState;
import com.nomagicsoftware.event.SupervisedService;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Links multiple {@link SupervisedService services} together, so that if one service dies, then
 * all die<br>
 * There are two modes:
 * <ul>
 *  <li>unsupervised: clients are responsible for managing {@link #services any services} and ensuring
 *      their death-callbacks properly utilize *this*</li>
 *  <li>supervised: manages the {@link #services} death-callbacks, so that upon the <em>death</em> of
 *      any, all will <em>die</em>.<br>
 *      <em>Completion</em> signifies that each {@literal service} has been {@link SupervisedService#stop() stopped},
 *      which may or may not imply that any/all of the services' threads are dead
 *  </li>
 * </ul>
 * @author thurston
 * @see Erlang's/link
 */
public class Link implements CompletionStage<Integer>
{
    final CompletableFuture<Integer> delegate = new CompletableFuture<>();
    final AtomicInteger done;
    final SupervisedService<?>[] services;
    
    public Link(int count)
    {
        this.done = new AtomicInteger(count);
        this.services = new SupervisedService<?>[0];
    }

    public Link(SupervisedService<?>... services)
    {
        this.done = new AtomicInteger(services.length);
        this.services = services;
        for (SupervisedService<?> service : services)
        {
            Consumer<DeathState> linked = (DeathState _ds) -> 
            {
                //for now we ignore the death-state since we're only implementing link
                System.err.println(String.format("In death callback on thread [%s]", Thread.currentThread()));
                //yes, we call stop() on ourselves, but that shouldn't be a problem?
                Arrays.spliterator(services).forEachRemaining(SupervisedService::stop);
                Link.this.update();
            };
            Consumer<DeathState> extant = (Consumer<DeathState>) service.getDeathCallback();
            if (null == extant)
                service.setDeathCallback(linked);
            else
                service.setDeathCallback(extant.andThen(linked)); //respect the precedence
            
        }
    }
    
    
    

    public boolean isDone()
    {
        return this.delegate.isDone();
    }

    public Integer get() throws InterruptedException, ExecutionException
    {
        return this.delegate.get();
    }

    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, 
                       TimeoutException
    {
        return this.delegate.get(timeout, unit);
    }

    public Integer join()
    {
        return this.delegate.join();
    }

    public Integer getNow(Integer valueIfAbsent)
    {
        return this.delegate.getNow(valueIfAbsent);
    }

//    public boolean complete(Integer value)
//    {
//        
//    }

    public boolean update()
    {
        if (0 == this.done.decrementAndGet())
            return this.delegate.complete(0);
        return false;
    }
    
    public boolean completeExceptionally(Throwable ex)
    {
        return this.delegate.completeExceptionally(ex);
    }

    @Override
    public <U> CompletableFuture<U> thenApply(Function<? super Integer, ? extends U> fn)
    {
        return delegate.thenApply(fn);
    }

    @Override
    public <U> CompletableFuture<U> thenApplyAsync(Function<? super Integer, ? extends U> fn)
    {
        return delegate.thenApplyAsync(fn);
    }

    @Override
    public <U> CompletableFuture<U> thenApplyAsync(Function<? super Integer, ? extends U> fn, Executor executor)
    {
        return delegate.thenApplyAsync(fn, executor);
    }

    @Override
    public CompletableFuture<Void> thenAccept(Consumer<? super Integer> action)
    {
        return delegate.thenAccept(action);
    }

    @Override
    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super Integer> action)
    {
        return delegate.thenAcceptAsync(action);
    }

    @Override
    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super Integer> action, Executor executor)
    {
        return delegate.thenAcceptAsync(action, executor);
    }

    @Override
    public CompletableFuture<Void> thenRun(Runnable action)
    {
        return delegate.thenRun(action);
    }

    @Override
    public CompletableFuture<Void> thenRunAsync(Runnable action)
    {
        return delegate.thenRunAsync(action);
    }

    @Override
    public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor)
    {
        return delegate.thenRunAsync(action, executor);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super Integer, ? super U, ? extends V> fn)
    {
        return delegate.thenCombine(other, fn);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super Integer, ? super U, ? extends V> fn)
    {
        return delegate.thenCombineAsync(other, fn);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super Integer, ? super U, ? extends V> fn, Executor executor)
    {
        return delegate.thenCombineAsync(other, fn, executor);
    }

    @Override
    public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super Integer, ? super U> action)
    {
        return delegate.thenAcceptBoth(other, action);
    }

    @Override
    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super Integer, ? super U> action)
    {
        return delegate.thenAcceptBothAsync(other, action);
    }

    @Override
    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super Integer, ? super U> action, Executor executor)
    {
        return delegate.thenAcceptBothAsync(other, action, executor);
    }

    @Override
    public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action)
    {
        return delegate.runAfterBoth(other, action);
    }

    @Override
    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action)
    {
        return delegate.runAfterBothAsync(other, action);
    }

    @Override
    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor)
    {
        return delegate.runAfterBothAsync(other, action, executor);
    }

    @Override
    public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends Integer> other, Function<? super Integer, U> fn)
    {
        return delegate.applyToEither(other, fn);
    }

    @Override
    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends Integer> other, Function<? super Integer, U> fn)
    {
        return delegate.applyToEitherAsync(other, fn);
    }

    @Override
    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends Integer> other, Function<? super Integer, U> fn, Executor executor)
    {
        return this.delegate.applyToEitherAsync(other, fn, executor);
    }

    @Override
    public CompletableFuture<Void> acceptEither(CompletionStage<? extends Integer> other, Consumer<? super Integer> action)
    {
        return this.delegate.acceptEither(other, action);
    }

    @Override
    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends Integer> other, Consumer<? super Integer> action)
    {
        return this.delegate.acceptEitherAsync(other, action);
    }

    @Override
    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends Integer> other, Consumer<? super Integer> action, Executor executor)
    {
        return this.delegate.acceptEitherAsync(other, action, executor);
    }

    @Override
    public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action)
    {
        return this.delegate.runAfterEither(other, action);
    }

    @Override
    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action)
    {
        return this.delegate.runAfterEitherAsync(other, action);
    }

    @Override
    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor)
    {
        return this.delegate.runAfterEitherAsync(other, action, executor);
    }

    @Override
    public <U> CompletableFuture<U> thenCompose(Function<? super Integer, ? extends CompletionStage<U>> fn)
    {
        return this.delegate.thenCompose(fn);
    }

    @Override
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super Integer, ? extends CompletionStage<U>> fn)
    {
        return this.delegate.thenComposeAsync(fn);
    }

    @Override
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super Integer, ? extends CompletionStage<U>> fn, Executor executor)
    {
        return this.delegate.thenComposeAsync(fn, executor);
    }

    @Override
    public CompletableFuture<Integer> whenComplete(BiConsumer<? super Integer, ? super Throwable> action)
    {
        return this.delegate.whenComplete(action);
    }

    @Override
    public CompletableFuture<Integer> whenCompleteAsync(BiConsumer<? super Integer, ? super Throwable> action)
    {
        return this.delegate.whenCompleteAsync(action);
    }

    @Override
    public CompletableFuture<Integer> whenCompleteAsync(BiConsumer<? super Integer, ? super Throwable> action, Executor executor)
    {
        return this.delegate.whenCompleteAsync(action, executor);
    }

    @Override
    public <U> CompletableFuture<U> handle(BiFunction<? super Integer, Throwable, ? extends U> fn)
    {
        return this.delegate.handle(fn);
    }

    @Override
    public <U> CompletableFuture<U> handleAsync(BiFunction<? super Integer, Throwable, ? extends U> fn)
    {
        return this.delegate.handleAsync(fn);
    }

    @Override
    public <U> CompletableFuture<U> handleAsync(BiFunction<? super Integer, Throwable, ? extends U> fn, Executor executor)
    {
        return this.delegate.handleAsync(fn, executor);
    }

    @Override
    public CompletableFuture<Integer> toCompletableFuture()
    {
        return this.delegate;
    }

    @Override
    public CompletableFuture<Integer> exceptionally(Function<Throwable, ? extends Integer> fn)
    {
        return this.delegate.exceptionally(fn);
    }

    
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return delegate.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled()
    {
        return delegate.isCancelled();
    }

    public boolean isCompletedExceptionally()
    {
        return delegate.isCompletedExceptionally();
    }

    public void obtrudeValue(Integer value)
    {
        delegate.obtrudeValue(value);
    }

    public void obtrudeException(Throwable ex)
    {
        delegate.obtrudeException(ex);
    }

    public int getNumberOfDependents()
    {
        return this.delegate.getNumberOfDependents();
    }
    
}
