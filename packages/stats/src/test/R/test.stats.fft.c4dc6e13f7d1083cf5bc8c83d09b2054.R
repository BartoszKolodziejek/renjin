library(hamcrest)

 expected <- c(0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i, 
0x1p+0 + 0x0p+0i, 0x1p+0 + 0x0p+0i) 
 

assertThat(stats:::fft(inverse=FALSE,z=c(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0))
,  identicalTo( expected, tol = 1e-6 ) )
