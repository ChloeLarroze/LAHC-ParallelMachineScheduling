import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import csv
from io import StringIO

#data sample:
# Instance,Iterations,ExecutionTime,LastImprovementIter,InitialMakespan,FinalMakespan,BestMakespan,ImprovementPercent
# 0,20,0,069,0,4,4,4,0,00
# 1,20,0,013,0,36,36,36,0,00
# 2,21,0,001,1,27,24,24,11,11


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

#stats with  proportion of improved solutions
def process_benchmark_stats(file_path):
    """
    Reads a CSV file, performs data type conversion, and calculates key statistics
    using pandas. The file path is passed as a function parameter.
    
    Args:
        file_path (str): The path to the CSV file (e.g., 'benchmark.csv').
    """
    try:
        df = pd.read_csv(file_path)
    except FileNotFoundError:
        print(f"Error: The file '{file_path}' was not found.")
        return
    except Exception as e:
        print(f"An error occurred while reading the CSV: {e}")
        return

    # Columns that need to be treated as numbers
    numeric_cols = ['Instance', 'Iterations', 'ExecutionTime', 'LastImprovementIter', 'InitialMakespan', 'FinalMakespan', 'BestMakespan', 'ImprovementPercent']
    for col in numeric_cols:
        # Convert to numeric, coercing errors (useful if non-numeric data creeps in)
        df[col] = pd.to_numeric(df[col], errors='coerce')

    # --- Statistics Calculation ---
    
    # Standard Descriptive Statistics
    descriptive_stats = df.describe()

    # Problem-Specific Statistics
    improved_instances = df[df['ImprovementPercent'] > 0.0]
    num_improved = len(improved_instances)
    total_instances = len(df)
    
    proportion_improved = num_improved / total_instances
    
    # Calculate mean only for instances where improvement was actually found
    mean_last_improvement_iter = df['LastImprovementIter'].mean() #improved_instances
    print(mean_last_improvement_iter)

    # --- Output ---
    
    print(f"\n--- Statistics for data read from '{file_path}' ---\n")
    
    stats_data = {
        "Statistic": [
            "Proportion of Improved Instances", 
            "Mean Last Improvement Iteration (Improved Only)"
        ],
        "Value": [
            proportion_improved, 
            mean_last_improvement_iter
        ]
    }
    stats_df = pd.DataFrame(stats_data)

    print("--- Problem-Specific Statistics ---")
    print(stats_df.to_markdown(index=False, floatfmt=".4f"))

    print("\n--- Standard Descriptive Statistics ---")
    print(descriptive_stats.to_markdown())



def __main__():
    csv_file = '/Users/chloelarroze/doc/S9/Java/LAHC-ParallelMachineScheduling/resources/out/benchmark#2_normalized.csv'
    #output_image = '/Users/chloelarroze/doc/S9/Java/LAHC-ParallelMachineScheduling/resources/makespan_over_iterations.png'
    #plot_makespan_over_iterations(csv_file)
    #output_image_avg = '/Users/chloelarroze/doc/S9/Java/LAHC-ParallelMachineScheduling/resources/average_makespan_improvement.png'
    #plot_average_makespan_improvement(csv_file)

    #process_benchmark_stats(csv_file)
    df = pd.read_csv(csv_file)
    print(df[' Iterations'].mean())


if __name__ == "__main__":
    __main__()