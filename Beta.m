function Y = Beta(N)
%This is the function that counts the number of 3-ribbon tilings of a
%3-by-N rectangle, under condition that these tiling do not have a vertical 
%tile.
global BB; %AA BB CC are vectors which cache the computed values of 
                  %Alpha, Beta, and Camma sequences.
if (N <= 1) %This value was initialized
    Y = BB(N + 1, 1);
    return;
end
if (N > 1) %need to check if we already computed it.
    if (BB(N + 1,1) > 0) %This value have already been calculated
        Y = BB(N + 1, 1);
        return;
    else   
        Y = Delta(N);
        for k = 0 : N - 2
            Y = Y + Delta(k) * Camma(N - k - 2);
            if (k < N - 2)
                Y = Y + Delta(k) * Camma(N - k - 3);
            end
        end  
        BB(N + 1, 1) = Y; %store this value of beta for future use;
    end
end
end