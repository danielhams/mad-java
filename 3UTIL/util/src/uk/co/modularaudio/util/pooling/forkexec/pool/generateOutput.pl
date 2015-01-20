#!/usr/bin/perl -w

use FileHandle;

## Heres where all the DB setup routines and stuff would go.

my($input) = "";

open( TRACEOUT, ">>/tmp/trace.out");
print TRACEOUT "STARTING\n";

while(<STDIN>)
{
    print TRACEOUT "I got a line '" . $_ . "'\n";
    TRACEOUT->autoflush(1);
    chop;
    $input = $_;
    ## input is the single string passed from the java code at this point
    ## so here is where the call to process that input goes.
    &processInputline($input);
    STDOUT->autoflush(1);
}

exit(0);

sub processInputline
{
    my($line) = @_;
    print ("The perl process got '" . $line . "' on stdin.\n");
    print ("********\n");
}
