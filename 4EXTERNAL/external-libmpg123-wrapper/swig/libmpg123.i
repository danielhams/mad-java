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

%native (HandRolled) void HandRolled(int, jstring jstr);
%{
    JNIEXPORT void JNICALL Java_uk_co_modularaudio_libmpg123wrapper_swig_libmpg123JNI_HandRolled(JNIEnv *, jclass, jint, jstring);
%}
