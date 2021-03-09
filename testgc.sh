rm ./test/*.c
rm ./out/myout/*.txt
rm ./out/cout/*.out
files=`ls test | grep .java`
#files="TestGc.java"
for filename in $files
do
    output=`java -cp bin Tiger "./test/$filename" -codegen C`
    gcc "./test/$filename.c" ./runtime/runtime.c  -o "./out/cout/${filename}.out" -g -m32 -w
    outret=`./out/cout/${filename}.out`
    echo "$outret" > "./out/myout/${filename}.txt"
done