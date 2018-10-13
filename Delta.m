function Y = Delta(N)
% This function counts the number of 3-ribbon tilings of the 3-by-N 
% rectangle that consists only of the horizontal tiles.
if (N < 0)
    Y = 0;
    return;
end
if (mod(N, 3) == 0)
    Y = 1;
else
    Y = 0;
end
end