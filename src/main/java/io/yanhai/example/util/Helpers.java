package io.yanhai.example.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yanhai
 */
public interface Helpers {

  static <A, B> Function<List<A>, List<B>> fmap(Function<A, B> mapper) {
    return as -> as.stream()
        .map(mapper)
        .collect(Collectors.toList());
  }

  static <A, B> Function<B, A> fconst(A a) {
    return b -> a;
  }
}
