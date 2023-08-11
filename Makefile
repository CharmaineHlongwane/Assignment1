# Assignment 1 makefile
# 06/08/2023
# Charmaine Hlongwane
#confirm if you shouldn't add the package info in your makefile
#Confirm that its not MP.MCMP.java
.SUFFIXES: .java .class
SRCDIR=src/MontePack
BINDIR=bin
DOCDIR=docs
JAVAC=/usr/bin/javac
JAVA=/usr/bin/java

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<

#list of the classes in order of dependency where the first in the list is the primary dependency
CLASSES2=Terrainarea.class SearchParallel.class MonteCarloMinimizationParallel.class 

CLASSES=$(CLASSES2:%.class=$(BINDIR)/%.class)

default: $(CLASSES)

# in green is the makefile command 
# used in terminal eg: make makemonte - will deploy the MCMP
# you want to change the name and the object that is being deployed 
runmonte: $(CLASSES)
	$(JAVA) -cp $(BINDIR) MonteCarloMinimizationParallel

#the docs command
docs:
	javadoc -d $(DOCDIR) $(SRCDIR)/*.java
# 
clean:
	rm $(BINDIR)/*.class
# it actively generates the javadocs
JAVAdocs: $(CLASSES)
	javadocs/./generate