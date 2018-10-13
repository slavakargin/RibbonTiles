function Y = Alpha(N) 
%This is the function that counts the number of 3-ribbon tilings of 
%a 3-by-N rectangle. 
global AA; %AA BB CC are vectors which cache the computed values of 
                  %Alpha, Beta, and Camma sequences.
if (N <= 1) %This value was initialized
    Y = AA(N + 1, 1);
    return;
end
if (N > 1) %need to check if we already computed it.
    if (AA(N + 1,1) > 0) %This value have already been calculated
        Y = AA(N + 1, 1);
        return;
    else    
        Y = Beta(N); %calculates the relevant B_N
        for k = 0 : N - 1
            Y = Y + Beta(k) * Alpha(N - k - 1);
        end
        AA(N + 1, 1) = Y; %store this value of Alpha for future use.
    end
end
end





