@echo off
cd src/main/resources/

for %%f in (*.lvl) do (
	7z a %%~nf.lvl.gz %%~nf.lvl
)

cd ../../..
