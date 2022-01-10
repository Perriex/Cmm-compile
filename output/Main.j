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
		ldc 1
		invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;
		astore_1
		ldc 0
		invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;
		astore_2
		aload_1
		invokevirtual java/lang/Boolean/booleanValue()Z
		aload_2
		invokevirtual java/lang/Boolean/booleanValue()Z
		iand
		invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;
		astore_3
		return
.end method
