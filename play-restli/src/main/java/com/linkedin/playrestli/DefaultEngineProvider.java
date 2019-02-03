package com.linkedin.playrestli;

import com.linkedin.parseq.Engine;
import com.linkedin.parseq.EngineBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.inject.ApplicationLifecycle;


@Singleton
public class DefaultEngineProvider implements EngineProvider {
  private int nThreads = Runtime.getRuntime().availableProcessors() + 1;
  private int terminationWaitSeconds = 1;
  private ExecutorService taskExecutor = Executors.newFixedThreadPool(nThreads);
  private ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor();
  private Engine engine = new EngineBuilder().setTaskExecutor(taskExecutor).setTimerScheduler(timerScheduler).build();

  @Inject
  public DefaultEngineProvider(ApplicationLifecycle applicationLifecycle) {
    applicationLifecycle.addStopHook(() -> CompletableFuture.runAsync(() -> {
      engine.shutdown();
      try {
        engine.awaitTermination(terminationWaitSeconds, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } finally {
        taskExecutor.shutdown();
        timerScheduler.shutdown();
      }
    }, ForkJoinPool.commonPool()));
  }

  @Override
  public Engine get() {
    return engine;
  }
}
