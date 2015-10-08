/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.grpc.async;

import static org.mockito.Mockito.when;
import io.grpc.Status;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.NanoClock;
import com.google.bigtable.v1.ReadRowsRequest;
import com.google.cloud.bigtable.config.RetryOptions;
import com.google.cloud.bigtable.grpc.scanner.ScanRetriesExhaustedException;

import static com.google.cloud.bigtable.config.RetryOptions.*;
/**
 * Test for {@link RetryingRpcFutureFallback}
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class RetryingRpcFutureFallbackTest {

  private RetryingRpcFutureFallback underTest;

  @Mock
  private RetryableRpc readAsync;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private NanoClock nanoClock;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    RetryOptions retryOptions =
        new RetryOptions(true, true, DEFAULT_INITIAL_BACKOFF_MILLIS, DEFAULT_BACKOFF_MULTIPLIER,
            DEFAULT_MAX_ELAPSED_BACKOFF_MILLIS, DEFAULT_STREAMING_BUFFER_SIZE,
            DEFAULT_READ_PARTIAL_ROW_TIMEOUT_MS) {
          @Override
          protected ExponentialBackOff.Builder createBackoffBuilder() {
            return super.createBackoffBuilder().setNanoClock(nanoClock);
          }
        };
    underTest =
        RetryingRpcFutureFallback.create(retryOptions, ReadRowsRequest.getDefaultInstance(),
          readAsync);
  }

  @Test
  public void testRuntimeException() throws Exception {
    expectedException.expect(ExecutionException.class);
    underTest.create(new RuntimeException("thrown")).get();
  }

  @Test
  public void testBackoff() throws Exception {
    expectedException.expect(ScanRetriesExhaustedException.class);

    final AtomicLong totalSleep = new AtomicLong();
    final long start = System.nanoTime();
    // We want the nanoClock to mimic the behavior of sleeping, but without the time penalty.
    // This will allow the RetryingRpcFutureFallback's ExponentialBackOff to work properly.
    // The ExponentialBackOff sends a BackOff.STOP only when the clock time reaches
    // start + maxElapsedTimeMillis.  Since we don't want to wait maxElapsedTimeMillis (60 seconds)
    // for the test to complete, we mock the clock.
    when(nanoClock.nanoTime()).then(new Answer<Long>() {
      @Override
      public Long answer(InvocationOnMock invocation) throws Throwable {
        return start + totalSleep.get();
      }
    });
    underTest.sleeper = new Sleeper() {
      @Override
      public void sleep(long ms) throws InterruptedException {
        totalSleep.addAndGet(ms * 1000000);
      }
    };
    // This should throw a ScanRetriesExhaustedException after a short while.  The max of 50
    // is a safe number of attempts before assuming that a ScanRetriesExhaustedException will
    // not be thrown.
    for (int i = 0; i < 50; i++) {
      underTest.create(Status.INTERNAL.asRuntimeException());
    }
  }
}
