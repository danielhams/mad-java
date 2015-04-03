%module libmpg123

/* simplifies how arrays are handled - see 21.8.3 'Wrapping C arrays with Java arrays' in the SWIG docs */

%include "carrays.i"
%array_class(float, CArrayFloat);
%array_class(double, CArrayDouble);
%array_class(int, CArrayInt);
%array_class(short, CArrayShort);
%array_class(long, CArrayLong);
%array_class(long long, CArrayLongLong);
%array_class(unsigned char, CArrayUnsignedChar);

%apply long long { off_t };

/* converts standard int types */
%include "stdint.i"
%include "windows.i"

%{
#include "mpg123.h"
%}

%include "mpg123.h"
