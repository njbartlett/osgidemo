package org.example.osgi.utils;

public interface Visitor<T> {
   void visit(T object);
}