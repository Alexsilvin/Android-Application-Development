package com.example.firstkot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private var students: MutableList<MainActivity.StudentGrade>,
    private val onItemClick: (int: Int) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val studentName: TextView = itemView.findViewById(R.id.studentName)
        private val studentMatricule: TextView = itemView.findViewById(R.id.studentMatricule)
        private val studentMarks: TextView = itemView.findViewById(R.id.studentMarks)
        private val studentGrade: TextView = itemView.findViewById(R.id.studentGrade)
        private val studentTotal: TextView = itemView.findViewById(R.id.studentTotal)
        private val gradeBadge: LinearLayout = itemView.findViewById(R.id.gradeBadge)

        fun bind(student: MainActivity.StudentGrade, position: Int) {
            studentName.text = student.name
            studentMatricule.text = "ID: ${student.matricule}"
            studentMarks.text = "CA: ${String.format("%.1f", student.caMark)} | Exam: ${String.format("%.1f", student.examMark)}"
            studentGrade.text = student.grade
            studentTotal.text = String.format("%.1f", student.total)

            itemView.setOnClickListener {
                onItemClick(position)
            }

            // Color grade badge based on letter grade
            val badgeColor = when (student.grade) {
                "A" -> itemView.context.getColor(android.R.color.holo_green_dark)
                "B+", "B" -> itemView.context.getColor(android.R.color.holo_green_light)
                "C+", "C" -> itemView.context.getColor(android.R.color.holo_orange_light)
                "D+", "D" -> itemView.context.getColor(android.R.color.holo_orange_dark)
                "F" -> itemView.context.getColor(android.R.color.holo_red_dark)
                else -> itemView.context.getColor(android.R.color.darker_gray)
            }
            gradeBadge.setBackgroundColor(badgeColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_list_item, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(students[position], position)
    }

    override fun getItemCount(): Int = students.size

    fun updateStudents(newStudents: MutableList<MainActivity.StudentGrade>) {
        students = newStudents
        notifyDataSetChanged()
    }
}
