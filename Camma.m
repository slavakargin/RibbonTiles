function Y = Camma(N)
%This function counts the number of 3 - ribbon tilings of the 
% region that starts as a staircase. The lowest row starts at 0, 
% the second row starts at 1, and the third row starts at 2.
% N denotes the number of squares in the third (top) row. 
% It is assumed that the tilings do not have vertical tiles. 

global CC; %AA BB CC are vectors which cache the computed values of 
                  %Alpha, Beta, and Camma sequences.
if (N <= 2) %This value was initialized
    Y = CC(N + 1, 1);
    return;
end
if (N > 2) %need to check if we already computed it.
    if (CC(N + 1,1) > 0) %This value have already been calculated
        Y = CC(N + 1, 1);
        return;
    else
        Y = Beta(N) + Beta(N - 1) + Camma(N - 3);
        CC(N + 1, 1) = Y; %store this value of Camma for future use;
    end
end
end