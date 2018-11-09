package com.linkedin.restli.server.play;

import com.linkedin.parseq.Engine;
import com.linkedin.parseq.EngineBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Provider;
import javax.inject.Singleton;


@Singleton
public class EngineProvider implements Provider<Engine> {
  private int nThreads = Runtime.getRuntime().availableProcessors() + 1;
  private ExecutorService taskExecutor = Executors.newFixedThreadPool(nThreads);
  private ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor();
  private Engine engine = new EngineBuilder().setTaskExecutor(taskExecutor).setTimerScheduler(timerScheduler).build();

  @Override
  public Engine get() {
    return engine;
  }
}
