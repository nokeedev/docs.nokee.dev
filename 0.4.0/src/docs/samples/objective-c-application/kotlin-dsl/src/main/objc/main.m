
#include <stdio.h>
#include "greeter.h"

int main(int argc, const char * argv[]) {
	Greeter* greeter = [Greeter alloc];
	printf("%s\n", [greeter sayHello:"Alice"]);
	return 0;
}