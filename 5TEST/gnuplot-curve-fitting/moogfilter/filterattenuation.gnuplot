set title "Moog Filter Attenuation"
set xlabel "Q"
set ylabel "Attenuation"
set grid
set xrange [-2.0:6.0]
set yrange [-2.0:2.0]
startOffset=0.23860180377960205
whatsLeft=1.0-startOffset
f4(x)=-whatsLeft*(((x/4.0)-1)**3)+startOffset
om=-0.75
dv=4.0
oo=0.25
f1(x) = om*(((x/dv)-1)**3)+oo 
fit [0.0:4.0] f1(x) "formatteddata.txt" using 1:2 via dv,oo,om
plot "formatteddata.txt" title "Measured", f2(x) title "Fit", f4(x) title "Hand Guess"