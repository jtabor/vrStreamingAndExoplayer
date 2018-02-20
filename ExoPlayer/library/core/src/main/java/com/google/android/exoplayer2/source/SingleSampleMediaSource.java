/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.source;

import android.net.Uri;
import android.os.Handler;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Assertions;
import java.io.IOException;

/**
 * Loads data at a given {@link Uri} as a single sample belonging to a single {@link MediaPeriod}.
 */
public final class SingleSampleMediaSource implements MediaSource {

  /**
   * Listener of {@link SingleSampleMediaSource} events.
   */
  public interface EventListener {

    /**
     * Called when an error occurs loading media data.
     *
     * @param sourceId The id of the reporting {@link SingleSampleMediaSource}.
     * @param e The cause of the failure.
     */
    void onLoadError(int sourceId, IOException e);

  }

  /**
   * The default minimum number of times to retry loading data prior to failing.
   */
  public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT = 3;

  private final Uri uri;
  private final DataSource.Factory dataSourceFactory;
  private final Format format;
  private final int minLoadableRetryCount;
  private final Handler eventHandler;
  private final EventListener eventListener;
  private final int eventSourceId;
  private final boolean treatLoadErrorsAsEndOfStream;
  private final Timeline timeline;

  /**
   * @param uri The {@link Uri} of the media stream.
   * @param dataSourceFactory The factory from which the {@link DataSource} to read the media will
   *     be obtained.
   * @param format The {@link Format} associated with the output track.
   * @param durationUs The duration of the media stream in microseconds.
   */
  public SingleSampleMediaSource(Uri uri, DataSource.Factory dataSourceFactory, Format format,
      long durationUs) {
    this(uri, dataSourceFactory, format, durationUs, DEFAULT_MIN_LOADABLE_RETRY_COUNT);
  }

  /**
   * @param uri The {@link Uri} of the media stream.
   * @param dataSourceFactory The factory from which the {@link DataSource} to read the media will
   *     be obtained.
   * @param format The {@link Format} associated with the output track.
   * @param durationUs The duration of the media stream in microseconds.
   * @param minLoadableRetryCount The minimum number of times to retry if a loading error occurs.
   */
  public SingleSampleMediaSource(Uri uri, DataSource.Factory dataSourceFactory, Format format,
      long durationUs, int minLoadableRetryCount) {
    this(uri, dataSourceFactory, format, durationUs, minLoadableRetryCount, null, null, 0, false);
  }

  /**
   * @param uri The {@link Uri} of the media stream.
   * @param dataSourceFactory The factory from which the {@link DataSource} to read the media will
   *     be obtained.
   * @param format The {@link Format} associated with the output track.
   * @param durationUs The duration of the media stream in microseconds.
   * @param minLoadableRetryCount The minimum number of times to retry if a loading error occurs.
   * @param eventHandler A handler for events. May be null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param eventSourceId An identifier that gets passed to {@code eventListener} methods.
   * @param treatLoadErrorsAsEndOfStream If true, load errors will not be propagated by sample
   *     streams, treating them as ended instead. If false, load errors will be propagated normally
   *     by {@link SampleStream#maybeThrowError()}.
   */
  public SingleSampleMediaSource(Uri uri, DataSource.Factory dataSourceFactory, Format format,
      long durationUs, int minLoadableRetryCount, Handler eventHandler, EventListener eventListener,
      int eventSourceId, boolean treatLoadErrorsAsEndOfStream) {
    this.uri = uri;
    this.dataSourceFactory = dataSourceFactory;
    this.format = format;
    this.minLoadableRetryCount = minLoadableRetryCount;
    this.eventHandler = eventHandler;
    this.eventListener = eventListener;
    this.eventSourceId = eventSourceId;
    this.treatLoadErrorsAsEndOfStream = treatLoadErrorsAsEndOfStream;
    timeline = new SinglePeriodTimeline(durationUs, true);
  }

  // MediaSource implementation.

  @Override
  public void prepareSource(ExoPlayer player, boolean isTopLevelSource, Listener listener) {
    listener.onSourceInfoRefreshed(this, timeline, null);
  }

  @Override
  public void maybeThrowSourceInfoRefreshError() throws IOException {
    // Do nothing.
  }

  @Override
  public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator) {
    Assertions.checkArgument(id.periodIndex == 0);
    return new SingleSampleMediaPeriod(uri, dataSourceFactory, format, minLoadableRetryCount,
        eventHandler, eventListener, eventSourceId, treatLoadErrorsAsEndOfStream);
  }

  @Override
  public void releasePeriod(MediaPeriod mediaPeriod) {
    ((SingleSampleMediaPeriod) mediaPeriod).release();
  }

  @Override
  public void releaseSource() {
    // Do nothing.
  }

}
