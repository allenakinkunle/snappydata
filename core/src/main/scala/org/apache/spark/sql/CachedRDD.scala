/*
 * Copyright (c) 2016 SnappyData, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package org.apache.spark.sql

import scala.reflect.ClassTag

import org.apache.spark.rdd.{MapPartitionsRDD, RDD}
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.{Partition, TaskContext}


private[sql] final class MapPartitionsPreserveRDD[U: ClassTag, T: ClassTag](
    prev: RDD[T], f: (TaskContext, Int, Iterator[T]) => Iterator[U],
    preservesPartitioning: Boolean = false)
    extends MapPartitionsRDD[U, T](prev, f, preservesPartitioning) {

  // TODO [sumedh] why doesn't the standard MapPartitionsRDD do this???
  override def getPreferredLocations(split: Partition) =
    firstParent[T].preferredLocations(split)
}

class DummyRDD(sqlContext: SQLContext)
    extends RDD[InternalRow](sqlContext.sparkContext, Nil) {

  /**
   * Implemented by subclasses to compute a given partition.
   */
  override def compute(split: Partition,
      context: TaskContext): Iterator[InternalRow] = Iterator.empty

  /**
   * Implemented by subclasses to return the set of partitions in this RDD.
   * This method will only be called once, so it is safe to implement
   * a time-consuming computation in it.
   */
  override protected def getPartitions: Array[Partition] = Array.empty
}
