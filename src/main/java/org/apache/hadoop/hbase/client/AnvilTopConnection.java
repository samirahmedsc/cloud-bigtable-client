// Because MasterKeepAliveConnection is default scope, we have to use this package.  :-/
package org.apache.hadoop.hbase.client;

import com.google.cloud.anviltop.hbase.AnvilTop;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.MasterKeepAliveConnection;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.protobuf.generated.MasterProtos;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.util.Threads;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
public class AnvilTopConnection implements HConnection {
  private static final Log LOG = LogFactory.getLog(AnvilTopConnection.class);

  private final Configuration conf;
  private volatile boolean closed;
  private volatile boolean aborted;
  private volatile ExecutorService batchPool = null;
  private User user = null;
  private volatile boolean cleanupPool = false;

  HConnectionManager mgr;

  public AnvilTopConnection(Configuration conf) {
    this.conf = conf;
    this.closed = false;
  }

  AnvilTopConnection(Configuration conf, boolean managed, ExecutorService pool, User user)
      throws IOException {
    this(conf);
    this.user = user;
    this.batchPool = pool;
    if (managed) {
      throw new IllegalArgumentException("AnvilTop does not support managed connections.");
    }
  }

  @Override
  public Configuration getConfiguration() {
    return this.conf;
  }

  @Override
  public HTableInterface getTable(String tableName) throws IOException {
    return getTable(TableName.valueOf(tableName));
  }

  @Override
  public HTableInterface getTable(byte[] tableName) throws IOException {
    return getTable(TableName.valueOf(tableName));
  }

  @Override
  public HTableInterface getTable(TableName tableName) throws IOException {
    return getTable(tableName, getBatchPool());
  }

  @Override
  public HTableInterface getTable(String tableName, ExecutorService pool) throws IOException {
    return getTable(TableName.valueOf(tableName), pool);
  }

  @Override
  public HTableInterface getTable(byte[] tableName, ExecutorService pool) throws IOException {
    return getTable(TableName.valueOf(tableName), pool);
  }

  @Override
  public HTableInterface getTable(TableName tableName, ExecutorService pool) throws IOException {
    HTableInterface hTableInterface = new AnvilTop(tableName);
    return hTableInterface;
  }

  @Override
  public boolean isMasterRunning() throws MasterNotRunningException, ZooKeeperConnectionException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isTableEnabled(TableName tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isTableEnabled(byte[] tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isTableDisabled(TableName tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isTableDisabled(byte[] tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isTableAvailable(TableName tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isTableAvailable(byte[] tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isTableAvailable(TableName tableName, byte[][] splitKeys) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isTableAvailable(byte[] tableName, byte[][] splitKeys) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HTableDescriptor[] listTables() throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public String[] getTableNames() throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public TableName[] listTableNames() throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HTableDescriptor getHTableDescriptor(TableName tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HTableDescriptor getHTableDescriptor(byte[] tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HRegionLocation locateRegion(TableName tableName, byte[] row) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HRegionLocation locateRegion(byte[] tableName, byte[] row) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void clearRegionCache() {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void clearRegionCache(TableName tableName) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void clearRegionCache(byte[] tableName) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void deleteCachedRegionLocation(HRegionLocation location) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HRegionLocation relocateRegion(TableName tableName, byte[] row) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HRegionLocation relocateRegion(byte[] tableName, byte[] row) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void updateCachedLocations(TableName tableName, byte[] rowkey, Object exception, HRegionLocation source) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void updateCachedLocations(byte[] tableName, byte[] rowkey, Object exception, HRegionLocation source) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HRegionLocation locateRegion(byte[] regionName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public List<HRegionLocation> locateRegions(TableName tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public List<HRegionLocation> locateRegions(byte[] tableName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public List<HRegionLocation> locateRegions(TableName tableName, boolean useCache, boolean offlined) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public List<HRegionLocation> locateRegions(byte[] tableName, boolean useCache, boolean offlined) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public MasterProtos.MasterService.BlockingInterface getMaster() throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public AdminProtos.AdminService.BlockingInterface getAdmin(ServerName serverName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public ClientProtos.ClientService.BlockingInterface getClient(ServerName serverName) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public AdminProtos.AdminService.BlockingInterface getAdmin(ServerName serverName, boolean getMaster) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HRegionLocation getRegionLocation(TableName tableName, byte[] row, boolean reload) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HRegionLocation getRegionLocation(byte[] tableName, byte[] row, boolean reload) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void processBatch(List<? extends Row> actions, TableName tableName, ExecutorService pool, Object[] results) throws IOException, InterruptedException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void processBatch(List<? extends Row> actions, byte[] tableName, ExecutorService pool, Object[] results) throws IOException, InterruptedException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public <R> void processBatchCallback(List<? extends Row> list, TableName tableName, ExecutorService pool, Object[] results, Batch.Callback<R> callback) throws IOException, InterruptedException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public <R> void processBatchCallback(List<? extends Row> list, byte[] tableName, ExecutorService pool, Object[] results, Batch.Callback<R> callback) throws IOException, InterruptedException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void setRegionCachePrefetch(TableName tableName, boolean enable) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void setRegionCachePrefetch(byte[] tableName, boolean enable) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean getRegionCachePrefetch(TableName tableName) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean getRegionCachePrefetch(byte[] tableName) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public int getCurrentNrHRS() throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HTableDescriptor[] getHTableDescriptorsByTableName(List<TableName> tableNames) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public HTableDescriptor[] getHTableDescriptors(List<String> tableNames) throws IOException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void clearCaches(ServerName sn) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public MasterKeepAliveConnection getKeepAliveMasterService() throws MasterNotRunningException {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public boolean isDeadServer(ServerName serverName) {
    throw new UnsupportedOperationException();  // TODO
  }

  @Override
  public void abort(final String msg, Throwable t) {
    if (t != null) {
      LOG.fatal(msg, t);
    } else {
      LOG.fatal(msg);
    }
    this.aborted = true;
    close();
    this.closed = true;
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public boolean isAborted(){
    return this.aborted;
  }

  @Override
  public void close() {
    if (this.closed) {
      return;
    }
    shutdownBatchPool();
    this.closed = true;
  }

  // Copied from org.apache.hadoop.hbase.client.HConnectionManager#getBatchPool()
  private ExecutorService getBatchPool() {
    if (batchPool == null) {
      // shared HTable thread executor not yet initialized
      synchronized (this) {
        if (batchPool == null) {
          int maxThreads = conf.getInt("hbase.hconnection.threads.max", 256);
          if (maxThreads == 0) {
            maxThreads = Runtime.getRuntime().availableProcessors() * 8;
          }
          long keepAliveTime = conf.getLong(
              "hbase.hconnection.threads.keepalivetime", 60);
          LinkedBlockingQueue<Runnable> workQueue =
              new LinkedBlockingQueue<Runnable>(128 *
                  conf.getInt("hbase.client.max.total.tasks", 200));
          this.batchPool = new ThreadPoolExecutor(
              maxThreads,
              maxThreads,
              keepAliveTime,
              TimeUnit.SECONDS,
              workQueue,
              Threads.newDaemonThreadFactory("hbase-connection-shared-executor"));
        }
        this.cleanupPool = true;
      }
    }
    return this.batchPool;
  }

  // Copied from org.apache.hadoop.hbase.client.HConnectionManager#shutdownBatchPool()
  private void shutdownBatchPool() {
    if (this.cleanupPool && this.batchPool != null && !this.batchPool.isShutdown()) {
      this.batchPool.shutdown();
      try {
        if (!this.batchPool.awaitTermination(10, TimeUnit.SECONDS)) {
          this.batchPool.shutdownNow();
        }
      } catch (InterruptedException e) {
        this.batchPool.shutdownNow();
      }
    }
  }
}