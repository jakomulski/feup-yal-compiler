.class public programa2
.super java/lang/Object


.method public static f1([I)[I
.limit locals 4
.limit stack 4
   iconst_0
  istore 2

    aload 0
   arraylength
  istore 3

    iload 3
   newarray int
  astore 1

  L0:

   iload 2
    aload 0
   arraylength
  if_icmpge L1

   aload 1
   iload 2
    aload 0
    iload 2
   iaload
  iastore

  iinc 2 1

  goto L0

  L1:

   aload 1
  areturn

.end method
.method public static f2(I)[I
.limit locals 2
.limit stack 7
   getstatic programa2/1 [I
    iload 0
   newarray int
  invokestatic programa2/&fill([II)V

   getstatic programa2/1 [I
   iconst_1
  invokestatic programa2/&fill([II)V

   aload 1
  areturn

.end method
.method public static main([Ljava/lang/String;)V
  .limit locals 1
  .limit stack 0
  invokestatic programa2/main()V
  return
.end method

.method public static main()V
.limit locals 4
.limit stack 3
    bipush 100
   newarray int
  astore 0

   aload 0
   iconst_0
   iconst_1
  iastore

   aload 0
   bipush 99
   iconst_2
  iastore

    aload 0
   invokestatic programa2/f1([I)[I
  astore 1

    aload 1
    iconst_0
   iaload
  istore 2

    aload 1
    bipush 99
   iaload
  istore 3

   ldc "first: "
   iload 2
  invokestatic io/println(Ljava/lang/String;I)V

   ldc "last: "
   iload 3
  invokestatic io/println(Ljava/lang/String;I)V

    bipush 100
   invokestatic programa2/f2(I)[I
  astore 1

    aload 1
    iconst_0
   iaload
  istore 2

    aload 1
    bipush 99
   iaload
  istore 3

   ldc "first: "
   iload 2
  invokestatic io/println(Ljava/lang/String;I)V

   ldc "last: "
   iload 3
  invokestatic io/println(Ljava/lang/String;I)V

  return

.end method

.method public static &fill([II)V
.limit locals 4
.limit stack 3
  iconst_0
  istore_2
  aload_0
  arraylength
  istore_3
 L5:
  iload_2
  iload_3
  if_icmpge L20
  aload_0
  iload_2
  iload_1
  iastore
  iinc 2 1
 goto L5
 L20:
  return
.end method

