echo "test ast =================="
files=`ls test | grep .java`
for filename in $files
do
    output=`java -cp bin Tiger "./test/$filename"`
    echo "./test/$filename"
    echo "========================"
    echo "$output"
done