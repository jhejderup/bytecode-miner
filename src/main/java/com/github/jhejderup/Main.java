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
    MethodScrapper visitor = new MethodScrapper(writer, reader.getClassName());
    reader.accept(visitor, ClassReader.SKIP_FRAMES);

    Files.write(Paths.get(args[1]), method_declerations, StandardCharsets.UTF_8,
        Files.exists(Paths.get(args[1])) ?
            StandardOpenOption.APPEND :
            StandardOpenOption.CREATE);

  }

  public final static class MethodScrapper extends ClassVisitor {

    private final String clazzName;

    public MethodScrapper(ClassWriter cw, String clazzName) {
      super(Opcodes.ASM7, cw);
      this.clazzName = clazzName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
        String signature, String[] exceptions) {
      MethodVisitor mv = super
          .visitMethod(access, name, desc, signature, exceptions);
      return new CallSiteScrapper(mv);
    }
  }

  public final static class CallSiteScrapper extends MethodVisitor {
    private final MethodVisitor mv;

    public CallSiteScrapper(MethodVisitor mv) {
      super(Opcodes.ASM7, mv);
      this.mv = mv;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
        String descriptor, boolean isInterface) {
      if (!isInterface)
        method_declerations.add(owner + "," + name + descriptor);
      super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
  }
}
