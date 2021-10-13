mvn clean install -Dmaven.test.skip=true
cd demo
mvn clean package
chmod +x run-demo.sh
cd ..
./run-demo.sh