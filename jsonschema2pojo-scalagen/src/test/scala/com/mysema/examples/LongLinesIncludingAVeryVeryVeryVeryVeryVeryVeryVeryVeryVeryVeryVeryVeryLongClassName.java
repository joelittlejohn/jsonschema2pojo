package com.mysema.examples;

public class LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName extends LongClassToExtendAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa {
	LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName x = new LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName(444, 2, 3, 4, 5, 6, 7, 8, 9, 10);
	
	public LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName(int... nums) {
	}
	
	public LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName(int a, int b, int c, int d, int e) {
		this(a, b, c, d, e, 1, 2, 3, 4, 5);
	}
	
	public void a(int... nums) {
		if ("very long condition ........................".length() > 0 || "other long condition".length() > 0) {
			a(555, 2, 3, 4, 5, 6, 7, 8, 9);
		}
		int[] aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa = nums;
		for (int n : aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa) {
			if ("long condition goes here ........................".length() > 0) {
				System.out.println(n);
			}
		}
	}
}

class LongClassToExtendAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa {
	public LongClassToExtendAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa() {
	}
	
	public LongClassToExtendAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa(int... nums) {
	}
}