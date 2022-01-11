.class public Main
.super java/lang/Object
.method public static main([Ljava/lang/String;)V
.limit stack 128
.limit locals 128
		new Main
		invokespecial Main/<init>()V
		return
.end method
.method public <init>()V
.limit stack 128
.limit locals 128
		aload_0
		invokespecial java/lang/Object/<init>()V
		iconst_0
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		astore_1
		iconst_0
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		astore_2
		ldc 1
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		ldc 4
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		isub
		dup
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		dup
		astore_1
		dup
		astore_2
		pop
		ldc 3
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		dup
		astore_1
		pop
		ldc 7
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		dup
		astore_2
		pop
		return
.end method
