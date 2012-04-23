package net.sf.jtmt.clustering;

/**
 * In fuzzy clustering, each point has a degree of belonging to clusters, 
 * as in fuzzy logic, rather than belonging completely to just one cluster. 
 * Thus, points on the edge of a cluster, may be in the cluster to a lesser 
 * degree than points in the center of cluster. For each point x we have a 
 * coefficient giving the degree of being in the kth cluster uk(x). Usually, 
 * the sum of those coefficients is defined to be 1:
 *     \forall x \sum_{k=1}^{\mathrm{num.}\ \mathrm{clusters}} u_k(x) \ =1.
 * With fuzzy c-means, the centroid of a cluster is the mean of all points, 
 * weighted by their degree of belonging to the cluster:
 *     \mathrm{center}_k = {{\sum_x u_k(x)^m x} \over {\sum_x u_k(x)^m}}.
 * The degree of belonging is related to the inverse of the distance to the 
 * cluster center:
 *     u_k(x) = {1 \over d(\mathrm{center}_k,x)},
 * then the coefficients are normalized and fuzzyfied with a real parameter 
 * m > 1 so that their sum is 1. So
 *     u_k(x) = \frac{1}{\sum_j \left(\frac{d(\mathrm{center}_k,x)}{d(\mathrm{center}_j,x)}\right)^{2/(m-1)}}.
 * For m equal to 2, this is equivalent to normalising the coefficient linearly 
 * to make their sum 1. When m is close to 1, then cluster center closest to the 
 * point is given much more weight than the others, and the algorithm is similar 
 * to k-means.
 * The fuzzy c-means algorithm is very similar to the k-means algorithm:
 *     * Choose a number of clusters.
 *     * Assign randomly to each point coefficients for being in the clusters.
 *     * Repeat until the algorithm has converged (that is, the coefficients' 
 *       change between two iterations is no more than ε, the given sensitivity 
 *       threshold) :
 *       o Compute the centroid for each cluster, using the formula above.
 *       o For each point, compute its coefficients of being in the clusters,
 *         using the formula above.
 * The algorithm minimizes intra-cluster variance as well, but has the same problems 
 * as k-means, the minimum is a local minimum, and the results depend on the initial 
 * choice of weights. The Expectation-maximization algorithm is a more statistically 
 * formalized method which includes some of these ideas: partial membership in classes. 
 * It has better convergence properties and is in general preferred to fuzzy-c-means.
 * @author Sujit Pal
 * @version $Revision$
 */
public class FuzzyCMeansClustering {

}
