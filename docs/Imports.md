# Imports

Has lexico similar to python To import file write ```import fileName```. To import specific functions or classes,
write ```import functionName, className from fileName```

## Using imported functions

You can directly write ```fileName.functionName``` to use imported function. For brief usage it is possible to
write ```functionName```. However:

* If current file has function with name ```functionName```, import will be shadowed;
* If two imported files have same imported name, such code will throw error.