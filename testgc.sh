rm ./out/myout/*.txt
rm ./out/cout/*.out
rm ./out/classout/*.class
files=`ls test | grep .java$`
#files="TestGc.java"
for filename in $files
do
    echo "$filename ==============================="
    output=`java -cp bin Tiger "./test/$filename" -codegen C`
    gcc "./test/$filename.c" ./runtime/runtime.c  -o "./out/cout/${filename}.out" -g -m32 -w
    outret=`./out/cout/${filename}.out @tiger -heapSize 750 -log false @`
    #echo "$outret" > "./out/myout/${filename}.txt"
    javac -d out/classout "./test/$filename"
    
    stdoutput=`java -classpath out/classout/ ${filename%.*}`
    #diff <(echo "$outret") <(echo "$stdoutput")
    if [ "$outret" = "$stdoutput" ];then
        echo "output right!"
    else 
        echo "output false!"
    fi
    echo "$filename ==============================="
done
rm ./test/*.c