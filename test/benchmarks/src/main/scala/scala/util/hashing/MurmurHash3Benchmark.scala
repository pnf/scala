package scala.util.hashing

import java.util.concurrent.TimeUnit
import scala.collection.immutable.ArraySeq

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@BenchmarkMode(Array(Mode.AverageTime))
@Fork(2)
@Threads(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class MurmurHash3Benchmark {

  @Param(Array("10", "100", "1000", "10000"))
  var size: Int = _
  var ordered: Array[Int] = _
  var mixed1: Array[Int] = _
  var mixed2: Array[Int] = _
  var range: Range = _

  @Setup(Level.Trial) def initNumbers: Unit = {
    range = (1 to size)
    ordered = Array.iterate(1, size)(_ + 1)
    assert(range == ArraySeq.unsafeWrapArray(ordered))
    assert(range.hashCode == ArraySeq.unsafeWrapArray(ordered).hashCode)
    assert(MurmurHash3.rangeHash(range.start, range.step, range.end, MurmurHash3.seqSeed) == MurmurHash3.rangeOptimizedArrayHash(ordered, MurmurHash3.seqSeed))
    mixed1 = Array.copyOf(ordered, ordered.length)
    mixed2 = Array.copyOf(ordered, ordered.length)
    swap(mixed1, 0, 1)
    swap(mixed2, mixed2.length-1, mixed2.length-2)
    assert(MurmurHash3.arrayHash(mixed1, MurmurHash3.seqSeed) == MurmurHash3.rangeOptimizedArrayHash(mixed1, MurmurHash3.seqSeed))
    assert(MurmurHash3.arrayHash(mixed2, MurmurHash3.seqSeed) == MurmurHash3.rangeOptimizedArrayHash(mixed2, MurmurHash3.seqSeed))
  }

  def swap(a: Array[Int], i1: Int, i2: Int): Unit = {
    val tmp = a(i1)
    a(i1) = a(i2)
    a(i2) = tmp
  }

  @Benchmark def A_rangeOptimizedArrayHashOrdered(bh: Blackhole): Unit = {
    val h = MurmurHash3.rangeOptimizedArrayHash(ordered, MurmurHash3.seqSeed)
    bh.consume(h)
  }

  @Benchmark def B_arrayHashOrdered(bh: Blackhole): Unit = {
    val h = MurmurHash3.arrayHash(ordered, MurmurHash3.seqSeed)
    bh.consume(h)
  }

  @Benchmark def rangeHash(bh: Blackhole): Unit = {
    val h = MurmurHash3.rangeHash(1, 1, size, MurmurHash3.seqSeed)
    bh.consume(h)
  }

  @Benchmark def rangeOptimizedArrayHashMixed1(bh: Blackhole): Unit = {
    val h = MurmurHash3.rangeOptimizedArrayHash(mixed1, MurmurHash3.seqSeed)
    bh.consume(h)
  }

  @Benchmark def rangeOptimizedArrayHashMixed2(bh: Blackhole): Unit = {
    val h = MurmurHash3.rangeOptimizedArrayHash(mixed2, MurmurHash3.seqSeed)
    bh.consume(h)
  }
}
