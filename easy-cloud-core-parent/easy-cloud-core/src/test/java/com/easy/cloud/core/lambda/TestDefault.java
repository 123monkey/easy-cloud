package com.easy.cloud.core.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author daiqi
 * @create 2018-11-17 10:32
 */
public interface TestDefault {
    static void test() {
        System.out.println("hello word");
    }

    default void test1() {
        System.out.println("test1");
    }

    class TestDefaultChild implements TestDefault{
        @Override
        public void test1() {
            System.out.println("lalala");
        }
    }

    static void main(String[] args) {

        Stream<Integer> stream = Stream.of(1,2,3,4);
        List<Integer> integerList = stream.collect(Collectors.toCollection(ArrayList::new));
        integerList.forEach(c -> System.out.println(c));


        test();
        new TestDefaultChild().test1();
    }
}
