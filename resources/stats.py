import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


# Function to plot makespan over iterations
def plot_makespan_over_iterations(csv_file):
    # Read the CSV file
    data = pd.read_csv(csv_file)

    # Plotting
    plt.figure(figsize=(10, 6))
    plt.plot(np.arange(len(data)), data['BestMakespan'], marker='o', linestyle='-')
    plt.title('Best Makespan Over Iterations')
    plt.xlabel('Iteration Count')
    plt.ylabel('Best Makespan')
    plt.grid(True)
    #plt.savefig(output_image)
    plt.show()

# Average makespan improvement over iterations
def plot_average_makespan_improvement(csv_file):
    # Read the CSV file
    data = pd.read_csv(csv_file)

    # Calculate average makespan improvement
    data['Improvement'] = data['InitialMakespan'] - data['BestMakespan']
    avg_improvement = data.groupby('Iterations')['Improvement'].mean().reset_index()

    # Plotting
    plt.figure(figsize=(10, 6))
    plt.scatter(avg_improvement['Iterations'], avg_improvement['Improvement'], marker='o')
    plt.title('Average Makespan Improvement Over Iterations')
    plt.xlabel('Iteration Count')
    plt.ylabel('Average Makespan Improvement')
    plt.grid(True)
    #plt.savefig(output_image)
    plt.show()


def __main__():
    csv_file = '/Users/chloelarroze/doc/S9/Java/LAHC-ParallelMachineScheduling/resources/out/benchmark#1.csv'
    #output_image = '/Users/chloelarroze/doc/S9/Java/LAHC-ParallelMachineScheduling/resources/makespan_over_iterations.png'
    plot_makespan_over_iterations(csv_file)
    #output_image_avg = '/Users/chloelarroze/doc/S9/Java/LAHC-ParallelMachineScheduling/resources/average_makespan_improvement.png'
    plot_average_makespan_improvement(csv_file)

if __name__ == "__main__":
    __main__()