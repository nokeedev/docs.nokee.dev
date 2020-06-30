
#include <iostream>
#include "greeter.h"

int main(int argc, const char * argv[]) {
	Greeter* greeter = [Greeter alloc];
	std::cout << [greeter sayHello:"Alice"] << std::endl;
	return 0;
}
