%module libsndfile

/* simplifies how arrays are handled - see 21.8.3 'Wrapping C arrays with Java arrays' in the SWIG docs */

%include "carrays.i"
%array_class(float, CArrayFloat);
%array_class(double, CArrayDouble);
%array_class(int, CArrayInt);
%array_class(short, CArrayShort);

/* converts the sf_count_t (__int64 typedef) to a long */
%include "stdint.i"

%{
#include "sndfile.h"
%}

%include "sndfile.h"
