package com.github.jhejderup;

import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static java.nio.file.Files.readAllBytes;

public class Main {

  private static final ArrayList<String> method_declerations;

  static {
    method_declerations = new ArrayList<>();
  }

  public static void main(String[] args) throws IOException {
    byte[] classfileBuffer = readAllBytes(Paths.get(args[0]));
    ClassReader reader = new ClassReader(classfileBuffer);
    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
    PrintMethodSignatures visitor = new PrintMethodSignatures(writer,
        reader.getClassName());
    reader.accept(visitor, ClassReader.SKIP_FRAMES);

    Files.write(Paths.get(args[1]), method_declerations, StandardCharsets.UTF_8,
        Files.exists(Paths.get(args[1])) ?
            StandardOpenOption.APPEND :
            StandardOpenOption.CREATE);

  }

  public final static class PrintMethodSignatures extends ClassVisitor {

    private final String clazzName;

    public PrintMethodSignatures(ClassWriter cw, String clazzName) {
      super(Opcodes.ASM7, cw);
      this.clazzName = clazzName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
        String signature, String[] exceptions) {
      method_declerations.add(clazzName + "/" + name + desc);
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
  }
}
