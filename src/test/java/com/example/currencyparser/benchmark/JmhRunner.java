package com.example.currencyparser.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JmhRunner {
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ParsingBenchmark.class.getSimpleName()) 
                .build();

        new Runner(opt).run();
    }
}
