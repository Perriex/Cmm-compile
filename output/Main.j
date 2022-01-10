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
		ldc 2
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		astore_1
		ldc 4
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		astore_2
		aload_1
		invokevirtual java/lang/Integer/intValue()I
		ldc 5
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		if_icmpge Label4
		ldc 1
		goto Label6
		Label4:
		ldc 0
		Label6:
		dup
		invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;
		astore_3
		return
.end method
