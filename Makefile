.PHONY: clean build install test test-coverage eclipse

build:
	sbt update
	sbt compile

clean:
	sbt clean

install:
	sbt one-jar

uninstall:
	echo "uninstall"

eclipse:
	sbt eclipse

test:
	sbt test
	
test-coverage:
	sbt clean
	sbt coverage:compile
	sbt coverage:test