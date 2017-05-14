package com.uchuhimo.layout

import com.lmax.disruptor.SingleProducerSequencer
import org.openjdk.jol.info.ClassLayout
import org.openjdk.jol.samples.*
import org.openjdk.jol.vm.VM

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
}
