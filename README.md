# OPTICS-Plugin
Clustering plugin for FlowJo built around the OPTICS algorithm

---

DISCLAIMER: Software distributed under the APACHE 2.0 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

THE SOFTWARE PROVIDED ON THE FLOWJO EXCHANGE SITE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL FLOWJO, LLC OR ANY OF THEIR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Without limiting the foregoing, FlowJo, LLC makes no warranty that:
1. the software will meet your requirements.
2. the software will be uninterrupted, timely, secure or error-free.
3. the results that may be obtained from the use of the software will be effective, accurate or reliable.
4. the quality of the software will meet your expectations.
5. any errors in the software obtained from the FlowJo Exchange web site will be corrected.

The use of the software downloaded through the FlowJo Exchange site is done at your own discretion and risk and with agreement that you will be solely responsible for any damage to your computer system or loss of data that results from such activities. No advice or information, whether oral or written, obtained by you from FlowJo, LLC, its its website, its contributors, employees, or contractors shall create any warranty for the software.

---

This is an example of a population plugin for FlowJo.
The full developer documentation is available here: https://www.gitbook.com/book/flowjo-lukej/optics-plugin-documentation/details

The OPTICS Algorithm was published in 1999 in a paper entitled "OPTICS: Ordering Points To Identify the Clustering Structure" by
Mihael Ankerst, Markus M. Breunig, Hans-Peter Kriegel, and Jörg Sander. 
Available here: http://fogo.dbs.ifi.lmu.de/Publikationen/Papers/OPTICS.pdf

The OPTICS algorithm re-orders the events and assigns a reachability distance in order to identify the structure of the clusters in the data. From this, the data can be sorted through manually, or run through a cluster detection algorithm. This implementation currently just splits the clusters based on peaks in the data, but other approaches are available.

One major weakness of this implementation is that it does not make use of a spatial index for the data points. A neighbor search currently takes O(n) time, and we need to do n neighbor searches. As Flow Cytometry data files can be extremely large, this is a problem that needs to be addressed in the future.

