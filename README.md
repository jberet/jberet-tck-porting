## configurations for running Jakarta Batch TCK with JBeret and WildFly.

### install and start WildFly standalone server
```bash
export JBOSS_HOME=...
cd $JBOSS_HOME/bin
./standalone.sh
```

### download and unzip Jakarta Batch TCK 2.1.0
```bash
wget https://download.eclipse.org/jakartaee/batch/2.1/jakarta.batch.official.tck-2.1.0.zip
unzip jakarta.batch.official.tck-2.1.0.zip
export BATCH_TCK_DIR=`pwd`/jakarta.batch.official.tck-2.1.0
```

### clone jberet-tck-porting repo, and build and deploy to WildFly to create test datasource and database
```bash
git clone https://github.com/jberet/jberet-tck-porting.git
export JBERET_PORTING_DIR=`pwd`/jberet-tck-porting
cd $JBERET_PORTING_DIR
mvn package
/bin/cp $JBERET_PORTING_DIR/target/jberet-tck-porting.jar $JBOSS_HOME/standalone/deployments/
```

### to customize the TCK for WildFly, copy configuration files from jberet-tck-porting repo to TCK
```bash
/bin/cp $JBERET_PORTING_DIR/src/main/resources/runners/sigtest/pom.xml $BATCH_TCK_DIR/runners/sigtest/pom.xml

/bin/cp $JBERET_PORTING_DIR/src/main/resources/runners/se-classpath/pom.xml $BATCH_TCK_DIR/runners/se-classpath/pom.xml

/bin/cp $JBERET_PORTING_DIR/src/main/resources/runners/platform-arquillian/pom.xml $BATCH_TCK_DIR/runners/platform-arquillian/pom.xml

/bin/cp $JBERET_PORTING_DIR/src/main/resources/runners/platform-arquillian/src/test/resources/arquillian.xml $BATCH_TCK_DIR/runners/platform-arquillian/src/test/resources/arquillian.xml
```

### to run TCK
#### run TCK signature tests
```bash
cd $BATCH_TCK_DIR/runners/sigtest/
mvn verify
```

#### run TCK in Java SE environment
```bash
cd $BATCH_TCK_DIR/runners/se-classpath/
mvn verify
```

#### run TCK in Jakarta EE environment 
```bash
cd $BATCH_TCK_DIR/runners/platform-arquillian/
mvn verify
```

### undeloy and shutdown
```bash
/bin/rm $JBOSS_HOME/standalone/deployments/jberet-tck-porting.jar
$JBOSS_HOME/bin/jboss-cli.sh --connect --command=shutdown
```