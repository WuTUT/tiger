
#######################################
adddec="void *Tiger_new(void *vtable, int size);

void *Tiger_new_array(int length);

int System_out_println(int i);"
#######################################
rm ./test/*.c
rm ./out/classout/*.class
rm ./out/cout/*.out
files=`ls test | grep .java`
#files="BinarySearch.java"
for filename in $files
do 
    echo "$filename ==============================="
    output=`java -cp bin Tiger "./test/$filename" -codegen C`
    #cfile=`cat <(echo "$adddec") "./test/$filename.c"`
    #echo "$cfile" > "./test/$filename.c"
    gcc "./test/$filename.c"  ./runtime/runtime.c -o "./out/cout/${filename}.out"
    outret=`./out/cout/${filename}.out`
    #echo "$outret"
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