all:
	rm -rf ./build
	lein uberjar
	cp -r ./target/uberjar/ ./build
	cp jobs.json ./build
	cp -r ./jobs ./build
	rm -r ./build/stale
	mkdir ./build/log
	mkdir ./build/last-runs
