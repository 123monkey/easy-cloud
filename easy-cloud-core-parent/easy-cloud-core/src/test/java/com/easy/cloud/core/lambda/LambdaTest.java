package com.easy.cloud.core.lambda;

import com.easy.cloud.core.common.json.utils.EcJSONUtils;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author daiqi
 * @create 2018-11-15 20:45
 */
public class LambdaTest {
    @Test
    public void testList() {
        List<String> list = Arrays.asList("1one", "two", "three", "4four");
        list.stream()
                .filter(str -> Character.isDigit(str.charAt(0)))
                .forEach((str -> System.out.println(str)));

        Set<String> set = list.stream()
                .filter(str -> !Character.isDigit(str.charAt(0)))
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        set.forEach(str -> {
            System.out.println(str);
        });

        list.forEach(str -> {
            if (str.equalsIgnoreCase("two")) {
                System.out.println("相等洛");
            }
        });

        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("hello", "helloValue1");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("hello", "helloValue2");

        mapList.add(map1);
        mapList.add(map2);

        mapList.forEach(map -> {
            map.forEach((k, v) -> {
                System.out.println("key：" + k + ",value" + v);
            });
        });

    }

    @Test
    public void testFlatMap() {
        Student obj1 = new Student();
        obj1.setName("mkyong");
        obj1.addBook("Java 8 in Action");
        obj1.addBook("Spring Boot in Action");
        obj1.addBook("Effective Java (2nd Edition)");
        obj1.addStudent(EcJSONUtils.parseObject(obj1, Student.class));
        Student obj2 = new Student();
        obj2.setName("zilap");
        obj2.addBook("Learning Python, 5th Edition");
        obj2.addBook("Effective Java (2nd Edition)");
        obj2.addStudent(EcJSONUtils.parseObject(obj2, Student.class));
        List<Student> list = new ArrayList<>();
        list.add(obj1);
        list.add(obj2);
        List<String> collect =
                list.stream()
                        .map(x -> x.getBook())      //Stream<Set<String>>
                        .flatMap(x -> x.stream())   //Stream<String>
                        .distinct()
                        .collect(Collectors.toList());
        collect.forEach(x -> System.out.println(x));

        List<String> collect1 =
                list.stream()
                        .map(x -> x.getBook())
                        .flatMap(x -> x.stream())
                        .distinct()
                        .collect(Collectors.toList());
        collect1.forEach(str -> System.out.println(str));


    }

    @Test
    public void test2() {
        List<Student> list = Arrays.asList(new Student("zhangsan"), new Student("Lisi"), new Student("sunchaowei"));
        long count = list.stream()
                .filter(student -> student.isFrom("Li"))
                .filter(student -> {
                    System.out.println("lalalla");
                    return student.isFrom("zh");
                })
                .count();
        System.out.println(count);
        Student student = list.stream()
                .min(Comparator.comparing(stu -> stu.name.length()))
                .get();
        System.out.println(student.name);
        System.out.println("--------------------------------------------");
        Set<Student> studentSet = list.stream()
                .filter(stu -> stu.name.length() > 4)
                .map(stu -> new Student(stu.name.toUpperCase()))
                .collect(Collectors.toSet());
        studentSet.forEach(student1 -> System.out.println(student1.name));

        Set<String> strings = list.stream()
                .map(str -> str.name.toUpperCase())

                .collect(Collectors.toSet());
        Set<String> names = list.stream()
                .filter(stu -> stu.name.startsWith("zhang"))
                .map(x -> x.getName())
                .collect(Collectors.toSet());
        names.forEach(name -> System.out.println(name));
    }

    @Test
    public void testReduce() {
        Integer [] numbers = {1,2,3,4,5,10,23};
        Stream<Integer> stream = Arrays.stream(numbers);
        System.out.println(stream.reduce(0, (cc, element) -> cc + element));
        int max = Arrays.stream(numbers)
                .max(Comparator.comparing(Function.identity()))
                .get();
        System.out.println(max);
        Student [] students = {new Student("zhangsan"), new Student("zhan1222gsan"), new Student("zhangsa212n")};
        Student student11 = Arrays.stream(students)
                .max(Comparator.comparing(Student::getName))
                .get();
        System.out.println(student11);

        Stream<Student> studentStream = Stream.of(new Student("zhangsan"), new Student("Lisi"));
        Stream<Student> studentStreamTemp = studentStream.filter(student -> student.getName().startsWith("zh"));
        studentStreamTemp.forEach(student -> System.out.println(student.name));

    }

    // 分割
    @Test
    public void testPart() {
        List<Student> list = new ArrayList<>();
        list.add(new Student("zhangsn=").perfect(true));
        list.add(new Student("zhangsn=").perfect(false));
        list.add(new Student("lisi1=").perfect(true));
        list.add(new Student("zhangsn=").perfect(false));
        list.add(new Student("lisi=").perfect(false));
        list.add(new Student("wangwu=").perfect(false));
        Map<Boolean, List<Student>> map = list.stream()
                .collect(Collectors.partitioningBy(stu -> stu.getPerfect()));
        map.forEach((k,v) -> System.out.println(k +":" + v));
        map.values().stream().collect(Collectors.toCollection(LinkedHashSet::new));


        Map<String, List<Student>> map1 = list.stream()
                .collect(Collectors.groupingBy(stu -> stu.getNameAndPerfect()));
        map1.values().forEach(stu -> System.out.println(stu));
        System.out.println("=========================");
        Map<String, List<String>> map2 = list.stream()
                .collect(Collectors.groupingBy(stu -> stu.getNameAndPerfect(),
                        Collectors.mapping(stu -> stu.getNameAndPerfect(), Collectors.toList())));

        map2.values().forEach(stu -> System.out.println(stu));
    }

    @Test
    public void testStr() {
        String [] strings ={"hello", "world",null,"hello2","hello3"};
        String s = Arrays.stream(strings)
                .filter(str -> str != null)
                .filter(str -> str.startsWith("he"))
                .filter( str -> !str.endsWith("lo"))
                .map(str ->  str.toUpperCase())
                .map(str -> str.toLowerCase())
                .map(str -> str + "AAAA")
                .collect(Collectors.joining(",", "[", "]"));

        System.out.println(s);
    }
    public class Student {
        private String name;
        private List<String> book;
        private List<Student> students;
        private Boolean perfect;

        public Student() {

        }

        public Student(String name) {
            this.name = name;
        }

        public void addBook(String book) {
            if (this.book == null) {
                this.book = new ArrayList<>();
            }
            if (this.students == null) {
                this.students = new ArrayList<>();
            }
            this.book.add(book);
        }

        public void addStudent(Student student) {
            if (this.students == null) {
                this.students = new ArrayList<>();
            }
            this.students.add(student);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getBook() {
            return book;
        }

        public void setBook(List<String> book) {
            this.book = book;
        }

        public List<Student> getStudents() {
            return students;
        }

        public void setStudents(List<Student> students) {
            this.students = students;
        }

        public boolean isFrom(String from) {
            if (this.name == null) {
                return false;
            }
            if (this.name.startsWith(from)) {
                return true;
            }
            return false;
        }

        public Boolean getPerfect() {
            return perfect;
        }

        public Student perfect(Boolean perfect) {
            this.perfect = perfect;
            return this;
        }

        public String getNameAndPerfect() {
            return this.name + this.getPerfect();
        }

        @Override
        public String toString() {
            return "Student{" +
                    "name='" + getNameAndPerfect() + '\'' +
                    '}';
        }
    }
}
