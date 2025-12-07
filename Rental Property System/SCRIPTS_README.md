## Prerequisites

- **Java 21** or later
- **Maven** 
- **Oracle Database** 

## Scripts

### 1. `setup.sh`
Checks dependencies and downloads all Maven dependencies.

```bash
./setup.sh
```

### 2. `build.sh` - Build Project
Compiles the project without running it.

```bash
./build.sh
```


### 3. `run.sh` - Run Project
Runs the application (requires prior build).

```bash
./run.sh
```


### 4. `build-and-run.sh` - Build and Run
Builds the project and immediately runs it.

```bash
./build-and-run.sh
```


### 5. `package.sh` - Create JAR
Creates a standalone JAR file for distribution.

```bash
./package.sh
```

**Output:** `target/rentalpropertysystem-1.0-SNAPSHOT.jar`

To run the packaged JAR:
```bash
java -jar target/rentalpropertysystem-1.0-SNAPSHOT.jar
```

### 6. `clean.sh` - Clean Build Artifacts
Removes all compiled files and build artifacts.

```bash
./clean.sh
```

# 2. Run setup to verify dependencies
./setup.sh

# 3. Build and run the application
./build-and-run.sh

**Course:** PDBe - Advanced Database Systems  
**Institution:** FIT VUT Brno  
**Project:** Rental Property Information System
