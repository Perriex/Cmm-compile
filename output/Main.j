.class public Main
.super java/lang/Object
.method public <init>()V
.limit stack 128
.limit locals 128
		aload_0
		invokespecial java/lang/Object/<init>()V
		return
.end method
.method public static main([Ljava/lang/String;)V
.limit stack 128
.limit locals 128
		new Main
		dup
		invokespecial Main/<init>()V
		astore_0
		iconst_0
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		astore_1
		iconst_0
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		astore_2
		aload_2
		invokevirtual java/lang/Integer/intValue()I
		aload_1
		invokevirtual java/lang/Integer/intValue()I
		iadd
		dup
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		astore_3
		return
.end method
