package com.uchuhimo.layout

import com.lmax.disruptor.SingleProducerSequencer
import org.openjdk.jol.info.ClassLayout
import org.openjdk.jol.samples.JOLSample_01_Basic
import org.openjdk.jol.samples.JOLSample_02_Alignment
import org.openjdk.jol.samples.JOLSample_03_Packing
import org.openjdk.jol.samples.JOLSample_04_Inheritance
import org.openjdk.jol.samples.JOLSample_05_InheritanceBarrier
import org.openjdk.jol.samples.JOLSample_06_Gaps
import org.openjdk.jol.samples.JOLSample_07_Exceptions
import org.openjdk.jol.samples.JOLSample_08_Class
import org.openjdk.jol.samples.JOLSample_09_Contended
import org.openjdk.jol.samples.JOLSample_10_DataModels
import org.openjdk.jol.samples.JOLSample_11_ClassWord
import org.openjdk.jol.samples.JOLSample_12_ThinLocking
import org.openjdk.jol.samples.JOLSample_13_BiasedLocking
import org.openjdk.jol.samples.JOLSample_14_FatLocking
import org.openjdk.jol.samples.JOLSample_15_IdentityHashCode
import org.openjdk.jol.samples.JOLSample_24_Difference
import org.openjdk.jol.samples.JOLSample_25_ArrayAlignment
import org.openjdk.jol.samples.JOLSample_26_Hotspot
import org.openjdk.jol.vm.VM
import java.util.concurrent.atomic.AtomicLong

data class TestFieldReorder(
        val a: Int, val b: Int,
        val c: Long, val d: Int)

class VolatileLong {
    @Volatile var value = 0L
    var p1: Long = 0
    var p2: Long = 0
    var p3: Long = 0
    var p4: Long = 0
    var p5: Long = 0
    var p6: Long = 0
}

fun main(args: Array<String>) {
    JOLSample_01_Basic.main(args)
    JOLSample_02_Alignment.main(args)
    JOLSample_03_Packing.main(args)
    JOLSample_04_Inheritance.main(args)
    JOLSample_05_InheritanceBarrier.main(args)
    JOLSample_06_Gaps.main(args)
    JOLSample_07_Exceptions.main(args)
    JOLSample_08_Class.main(args)
    JOLSample_09_Contended.main(args)

    println(VM.current().details())
    println(ClassLayout.parseClass(SingleProducerSequencer::class.java).toPrintable())

    JOLSample_10_DataModels.main(args)
    JOLSample_11_ClassWord.main(args)
    JOLSample_12_ThinLocking.main(args)
    JOLSample_13_BiasedLocking.main(args)
    JOLSample_14_FatLocking.main(args)
    JOLSample_15_IdentityHashCode.main(args)
//    JOLSample_16_AL_LL.main(args)
//    JOLSample_17_Allocation.main(args)
//    JOLSample_18_Layouts.main(args)
//    JOLSample_19_Promotion.main(args)

//    JOLSample_20_Roots.main(args)
//    JOLSample_21_Arrays.main(args)

//    JOLSample_22_Compaction.main(args)
//    JOLSample_23_Defragmentation.main(args)
//    JOLSample_24_Colocation.main(args)

    JOLSample_24_Difference.main(args)
    JOLSample_25_ArrayAlignment.main(args)
    JOLSample_26_Hotspot.main(args)

    println(ClassLayout.parseClass(TestFieldReorder::class.java).toPrintable())
    println(ClassLayout.parseClass(VolatileLong::class.java).toPrintable())
    println(ClassLayout.parseClass(VolatileLongInJava::class.java).toPrintable())
    println(ClassLayout.parseClass(AtomicLong::class.java).toPrintable())
    println(ClassLayout.parseClass(PaddedAtomicLong::class.java).toPrintable())
}
