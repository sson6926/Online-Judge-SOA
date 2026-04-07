import React, { memo, Component } from 'react';
import { Link } from 'react-router-dom';
import {
  Zap,
  Globe2,
  BarChart3,
  History,
  Trophy,
  Code2,
  CheckCircle2,
  XCircle,
  Clock,
  ArrowRight,
  Play,
  Send } from
'lucide-react';
import { useInViewAnimation } from '../hooks/useInViewAnimation';
// --- Reusable Components ---
interface LandingButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'tertiary';
  href?: string;
  className?: string;
  onClick?: () => void;
}
function LandingButton({
  children,
  variant = 'primary',
  href,
  className = '',
  onClick
}: LandingButtonProps) {
  const baseStyles =
  'px-7 py-3 rounded-full font-medium text-sm transition-all whitespace-nowrap inline-flex items-center justify-center';
  const variants = {
    primary:
    'bg-primary-dark text-white shadow-[0_4px_14px_0_rgba(5,26,36,0.39)] hover:shadow-[0_6px_20px_rgba(5,26,36,0.23)] hover:bg-dark transform hover:-translate-y-[1px]',
    secondary:
    'bg-white text-dark border border-slate-200 shadow-sm hover:bg-slate-50 hover:border-slate-300',
    tertiary:
    'bg-white text-dark shadow-[inset_0_0_0_1px_rgba(0,0,0,0.1)] hover:bg-slate-50'
  };
  const combinedClassName = `${baseStyles} ${variants[variant]} ${className}`;
  if (href) {
    return (
      <Link to={href} className={combinedClassName} onClick={onClick}>
        {children}
      </Link>);

  }
  return (
    <button className={combinedClassName} onClick={onClick}>
      {children}
    </button>);

}
function AnimatedSection({
  children,
  className = '',
  delay = 0




}: {children: React.ReactNode;className?: string;delay?: number;}) {
  const { ref, isInView } = useInViewAnimation();
  return (
    <div
      ref={ref}
      className={`${className} ${isInView ? 'animate-fade-in-up' : 'opacity-0'}`}
      style={{
        animationDelay: `${delay}s`
      }}>
      
      {children}
    </div>);

}
// --- Main Landing Page ---
export function Landing() {
  return (
    <div className="min-h-screen bg-white text-dark selection:bg-muted-light selection:text-primary-dark overflow-x-hidden">
      {/* 1. HERO SECTION */}
      <section className="pt-24 md:pt-32 pb-16 px-6 max-w-[640px] mx-auto text-center flex flex-col items-center">
        <AnimatedSection delay={0}>
          <div className="inline-flex items-center justify-center mb-8">
            <span className="font-accent text-3xl font-semibold tracking-tight text-primary-dark">
              CodeArena
            </span>
          </div>
        </AnimatedSection>

        <AnimatedSection delay={0.1}>
          <p className="font-mono text-xs font-medium text-muted-dark uppercase tracking-widest mb-6">
            Online Judge for serious developers
          </p>
        </AnimatedSection>

        <AnimatedSection delay={0.2}>
          <h1 className="text-5xl md:text-6xl font-medium tracking-tight leading-[1.1] mb-8 text-dark">
            Master{' '}
            <span className="font-accent text-primary-dark">real problems</span>
            , not just theory.
          </h1>
        </AnimatedSection>

        <AnimatedSection delay={0.3}>
          <div className="space-y-4 text-lg text-muted-dark leading-relaxed mb-10 max-w-[520px]">
            <p>
              A platform designed to help developers practice algorithm and
              system thinking with uncompromising precision.
            </p>
            <p>
              Experience near real-time judging, support for 20+ languages, and
              fast feedback loops that accelerate your learning.
            </p>
            <p>
              Built specifically for competitive programmers and engineers
              preparing for top-tier technical interviews.
            </p>
          </div>
        </AnimatedSection>

        <AnimatedSection
          delay={0.4}
          className="flex flex-col sm:flex-row items-center gap-4 w-full sm:w-auto">
          
          <LandingButton
            href="/signup"
            variant="primary"
            className="w-full sm:w-auto">
            
            Start solving
          </LandingButton>
          <LandingButton
            href="/problems"
            variant="secondary"
            className="w-full sm:w-auto">
            
            Browse problems
          </LandingButton>
        </AnimatedSection>
      </section>

      {/* 2. PROBLEM PREVIEW MARQUEE */}
      <section className="py-16 overflow-hidden border-y border-slate-100 bg-slate-50/50">
        <div className="flex w-[200%] md:w-max animate-marquee-mobile md:animate-marquee pause-on-hover">
          {/* First set */}
          <div className="flex gap-6 px-3">
            <MarqueeCard
              title="Two Sum"
              difficulty="Easy"
              status="Accepted"
              lang="Python" />
            
            <MarqueeCard
              title="LRU Cache"
              difficulty="Medium"
              status="Accepted"
              lang="Java" />
            
            <MarqueeCard
              title="Median of Arrays"
              difficulty="Hard"
              status="Wrong Answer"
              lang="C++" />
            
            <MarqueeCard
              title="Merge Intervals"
              difficulty="Medium"
              status="Accepted"
              lang="Go" />
            
            <MarqueeCard
              title="Trapping Rain Water"
              difficulty="Hard"
              status="Time Limit Exceeded"
              lang="Python" />
            
          </div>
          {/* Duplicated set for seamless loop */}
          <div className="flex gap-6 px-3">
            <MarqueeCard
              title="Two Sum"
              difficulty="Easy"
              status="Accepted"
              lang="Python" />
            
            <MarqueeCard
              title="LRU Cache"
              difficulty="Medium"
              status="Accepted"
              lang="Java" />
            
            <MarqueeCard
              title="Median of Arrays"
              difficulty="Hard"
              status="Wrong Answer"
              lang="C++" />
            
            <MarqueeCard
              title="Merge Intervals"
              difficulty="Medium"
              status="Accepted"
              lang="Go" />
            
            <MarqueeCard
              title="Trapping Rain Water"
              difficulty="Hard"
              status="Time Limit Exceeded"
              lang="Python" />
            
          </div>
        </div>
      </section>

      {/* 3. FEATURES SECTION */}
      <section className="py-24 px-6 max-w-7xl mx-auto">
        <AnimatedSection>
          <h2 className="text-3xl font-medium tracking-tight text-center mb-16">
            Engineered for <span className="font-accent">performance</span>
          </h2>
        </AnimatedSection>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <FeatureCard
            icon={<Zap className="w-5 h-5" />}
            title="Fast judging"
            description="Distributed architecture ensures near real-time execution and feedback for your submissions."
            delay={0.1} />
          
          <FeatureCard
            icon={<Globe2 className="w-5 h-5" />}
            title="Multi-language support"
            description="Write solutions in Java, Python, C++, Rust, Go, JavaScript, and 15+ other languages."
            delay={0.2} />
          
          <FeatureCard
            icon={<BarChart3 className="w-5 h-5" />}
            title="Detailed results"
            description="Analyze your code with precise runtime metrics, memory usage, and granular testcase feedback."
            delay={0.3} />
          
          <FeatureCard
            icon={<History className="w-5 h-5" />}
            title="Submission history"
            description="Track your progress over time, revisit past solutions, and learn from your mistakes."
            delay={0.4} />
          
          <FeatureCard
            icon={<Trophy className="w-5 h-5" />}
            title="Global ranking"
            description="Compete with developers worldwide. Climb the leaderboard and earn your competitive edge."
            delay={0.5} />
          
          <FeatureCard
            icon={<Code2 className="w-5 h-5" />}
            title="Clean code editor"
            description="A distraction-free, VSCode-like editing experience with syntax highlighting and auto-indent."
            delay={0.6} />
          
        </div>
      </section>

      {/* 4. PROBLEM + EDITOR PREVIEW SECTION */}
      <section className="py-24 bg-primary-dark text-white overflow-hidden">
        <div className="max-w-7xl mx-auto px-6">
          <AnimatedSection>
            <div className="text-center mb-16">
              <h2 className="text-3xl font-medium tracking-tight mb-4">
                A workspace built for <span className="font-accent">focus</span>
              </h2>
              <p className="text-muted-light max-w-2xl mx-auto">
                Everything you need to solve complex problems, right where you
                need it.
              </p>
            </div>
          </AnimatedSection>

          <AnimatedSection delay={0.2}>
            <div className="flex flex-col lg:flex-row rounded-2xl overflow-hidden border border-muted-dark bg-[#0a192f] shadow-2xl">
              {/* Left: Problem Statement */}
              <div className="w-full lg:w-5/12 p-8 border-b lg:border-b-0 lg:border-r border-muted-dark bg-[#0d212c]">
                <div className="flex items-center gap-3 mb-6">
                  <span className="px-2.5 py-1 rounded-md bg-emerald-500/10 text-emerald-400 text-xs font-medium border border-emerald-500/20">
                    Easy
                  </span>
                  <h3 className="text-xl font-medium">1. Two Sum</h3>
                </div>
                <div className="space-y-4 text-sm text-muted-light/80 leading-relaxed">
                  <p>
                    Given an array of integers{' '}
                    <code className="bg-muted-dark/50 px-1.5 py-0.5 rounded text-white font-mono text-xs">
                      nums
                    </code>{' '}
                    and an integer{' '}
                    <code className="bg-muted-dark/50 px-1.5 py-0.5 rounded text-white font-mono text-xs">
                      target
                    </code>
                    , return indices of the two numbers such that they add up to{' '}
                    <code className="bg-muted-dark/50 px-1.5 py-0.5 rounded text-white font-mono text-xs">
                      target
                    </code>
                    .
                  </p>
                  <p>
                    You may assume that each input would have exactly one
                    solution, and you may not use the same element twice.
                  </p>

                  <div className="mt-6">
                    <p className="font-medium text-white mb-2">Example 1:</p>
                    <div className="bg-[#051a24] p-4 rounded-lg font-mono text-xs border border-muted-dark">
                      <div className="text-muted-light">
                        <span className="text-slate-400">Input:</span> nums =
                        [2,7,11,15], target = 9
                      </div>
                      <div className="text-muted-light mt-1">
                        <span className="text-slate-400">Output:</span> [0,1]
                      </div>
                      <div className="text-muted-light mt-1">
                        <span className="text-slate-400">Explanation:</span>{' '}
                        Because nums[0] + nums[1] == 9, we return [0, 1].
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right: Code Editor Mock */}
              <div className="w-full lg:w-7/12 flex flex-col bg-[#1e1e1e]">
                <div className="flex items-center justify-between px-4 py-3 bg-[#252526] border-b border-[#333]">
                  <div className="flex items-center gap-2">
                    <div className="px-3 py-1 rounded bg-[#3c3c3c] text-xs font-mono text-slate-200 border border-[#444]">
                      Python 3
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button className="px-3 py-1.5 rounded bg-[#3c3c3c] text-slate-200 text-xs font-medium flex items-center gap-1.5 hover:bg-[#4c4c4c] transition-colors">
                      <Play className="w-3 h-3" /> Run
                    </button>
                    <button className="px-3 py-1.5 rounded bg-emerald-600 text-white text-xs font-medium flex items-center gap-1.5 hover:bg-emerald-500 transition-colors">
                      <Send className="w-3 h-3" /> Submit
                    </button>
                  </div>
                </div>
                <div className="flex-1 p-4 font-mono text-sm leading-relaxed overflow-hidden">
                  <div className="flex">
                    <div className="text-[#858585] text-right pr-4 select-none flex flex-col">
                      <span>1</span>
                      <span>2</span>
                      <span>3</span>
                      <span>4</span>
                      <span>5</span>
                    </div>
                    <div className="text-[#d4d4d4] flex flex-col">
                      <span>
                        <span className="text-[#569cd6]">class</span>{' '}
                        <span className="text-[#4ec9b0]">Solution</span>:
                      </span>
                      <span>
                        {' '}
                        <span className="text-[#569cd6]">def</span>{' '}
                        <span className="text-[#dcdcaa]">twoSum</span>(
                        <span className="text-[#9cdcfe]">self</span>,{' '}
                        <span className="text-[#9cdcfe]">nums</span>:{' '}
                        <span className="text-[#4ec9b0]">List</span>[
                        <span className="text-[#4ec9b0]">int</span>],{' '}
                        <span className="text-[#9cdcfe]">target</span>:{' '}
                        <span className="text-[#4ec9b0]">int</span>){' '}
                        <span className="text-[#569cd6]">{'->'}</span>{' '}
                        <span className="text-[#4ec9b0]">List</span>[
                        <span className="text-[#4ec9b0]">int</span>]:
                      </span>
                      <span>
                        {' '}
                        <span className="text-[#6a9955]">
                          # Write your code here
                        </span>
                      </span>
                      <span>
                        {' '}
                        <span className="text-[#c586c0]">pass</span>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </AnimatedSection>
        </div>
      </section>

      {/* 5. SUBMISSIONS TABLE PREVIEW */}
      <section className="py-24 px-6 max-w-5xl mx-auto">
        <AnimatedSection>
          <div className="text-center mb-12">
            <h2 className="text-3xl font-medium tracking-tight mb-4">
              Track your <span className="font-accent">progress</span>
            </h2>
            <p className="text-muted-dark">
              Detailed insights into every line of code you submit.
            </p>
          </div>
        </AnimatedSection>

        <AnimatedSection delay={0.2}>
          <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-xs text-muted-dark uppercase bg-slate-50 border-b border-slate-200">
                  <tr>
                    <th className="px-6 py-4 font-medium">Time</th>
                    <th className="px-6 py-4 font-medium">Problem</th>
                    <th className="px-6 py-4 font-medium">Status</th>
                    <th className="px-6 py-4 font-medium">Runtime</th>
                    <th className="px-6 py-4 font-medium">Language</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  <tr className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 text-muted-dark whitespace-nowrap">
                      2 mins ago
                    </td>
                    <td className="px-6 py-4 font-medium text-dark">Two Sum</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1.5 text-emerald-600 font-medium">
                        <CheckCircle2 className="w-4 h-4" /> Accepted
                      </span>
                    </td>
                    <td className="px-6 py-4 font-mono text-muted-dark">
                      45 ms
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2 py-1 bg-slate-100 rounded text-xs text-muted-dark font-medium">
                        Python
                      </span>
                    </td>
                  </tr>
                  <tr className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 text-muted-dark whitespace-nowrap">
                      15 mins ago
                    </td>
                    <td className="px-6 py-4 font-medium text-dark">
                      LRU Cache
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1.5 text-red-600 font-medium">
                        <XCircle className="w-4 h-4" /> Wrong Answer
                      </span>
                    </td>
                    <td className="px-6 py-4 font-mono text-muted-dark">N/A</td>
                    <td className="px-6 py-4">
                      <span className="px-2 py-1 bg-slate-100 rounded text-xs text-muted-dark font-medium">
                        Java
                      </span>
                    </td>
                  </tr>
                  <tr className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 text-muted-dark whitespace-nowrap">
                      1 hour ago
                    </td>
                    <td className="px-6 py-4 font-medium text-dark">
                      Trapping Rain Water
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1.5 text-amber-600 font-medium">
                        <Clock className="w-4 h-4" /> Time Limit Exceeded
                      </span>
                    </td>
                    <td className="px-6 py-4 font-mono text-muted-dark">
                      {'>'} 2000 ms
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2 py-1 bg-slate-100 rounded text-xs text-muted-dark font-medium">
                        C++
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div className="px-6 py-4 border-t border-slate-200 bg-slate-50 flex justify-between items-center text-sm text-muted-dark">
              <span>Showing 1 to 3 of 142 entries</span>
              <div className="flex gap-2">
                <button className="px-3 py-1 border border-slate-200 rounded hover:bg-white disabled:opacity-50">
                  Prev
                </button>
                <button className="px-3 py-1 border border-slate-200 rounded bg-white shadow-sm">
                  1
                </button>
                <button className="px-3 py-1 border border-slate-200 rounded hover:bg-white">
                  2
                </button>
                <button className="px-3 py-1 border border-slate-200 rounded hover:bg-white">
                  Next
                </button>
              </div>
            </div>
          </div>
        </AnimatedSection>
      </section>

      {/* 6. TESTIMONIAL SECTION */}
      <section className="py-24 bg-slate-50 border-y border-slate-100">
        <div className="max-w-4xl mx-auto px-6 text-center">
          <AnimatedSection>
            <h2 className="text-2xl md:text-4xl font-medium leading-tight mb-16 text-dark max-w-3xl mx-auto">
              "This platform helped me build the{' '}
              <span className="font-accent text-primary-dark">real skills</span>{' '}
              I needed to land my first{' '}
              <span className="font-accent text-primary-dark">backend job</span>
              ."
            </h2>
          </AnimatedSection>

          <div className="flex flex-wrap justify-center gap-8">
            <TestimonialAvatar
              name="Sarah J."
              role="Software Engineer"
              delay={0.2} />
            
            <TestimonialAvatar
              name="Michael T."
              role="Backend Developer"
              delay={0.3} />
            
            <TestimonialAvatar
              name="Elena R."
              role="Systems Engineer"
              delay={0.4} />
            
          </div>
        </div>
      </section>

      {/* 7. USE CASE SECTION */}
      <section className="py-24 px-6 max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="text-center mb-16">
            <h2 className="text-3xl font-medium tracking-tight mb-4">
              Built for every stage
            </h2>
          </div>
        </AnimatedSection>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <UseCaseCard title="Interview preparation" delay={0.1} />
          <UseCaseCard title="Competitive programming" delay={0.2} />
          <UseCaseCard title="University training" delay={0.3} />
          <UseCaseCard title="Company hiring tests" delay={0.4} />
        </div>
      </section>

      {/* 8. CTA SECTION */}
      <section className="py-32 px-6 bg-primary-dark text-white text-center relative overflow-hidden">
        {/* Subtle background pattern */}
        <div
          className="absolute inset-0 opacity-10"
          style={{
            backgroundImage: 'radial-gradient(#ffffff 1px, transparent 1px)',
            backgroundSize: '32px 32px'
          }}>
        </div>

        <div className="relative z-10 max-w-2xl mx-auto">
          <AnimatedSection>
            <h2 className="text-4xl md:text-5xl font-medium tracking-tight mb-8">
              Start solving like a{' '}
              <span className="font-accent">real engineer</span>.
            </h2>
          </AnimatedSection>
          <AnimatedSection
            delay={0.2}
            className="flex flex-col sm:flex-row justify-center gap-4">
            
            <LandingButton
              href="/signup"
              className="bg-white text-primary-dark hover:bg-slate-100">
              
              Create account
            </LandingButton>
            <LandingButton
              href="/login"
              className="bg-transparent text-white border border-white/20 hover:bg-white/10">
              
              Login
            </LandingButton>
          </AnimatedSection>
        </div>
      </section>

      {/* 9. FOOTER */}
      <footer className="py-12 px-6 border-t border-slate-200 text-sm text-muted-dark">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex items-center gap-2">
            <span className="font-accent text-xl font-semibold text-primary-dark">
              CodeArena
            </span>
          </div>

          <div className="flex flex-wrap justify-center gap-6 md:gap-8">
            <Link to="/problems" className="hover:text-dark transition-colors">
              Problems
            </Link>
            <Link
              to="/submissions"
              className="hover:text-dark transition-colors">
              
              Submissions
            </Link>
            <Link to="/login" className="hover:text-dark transition-colors">
              Login
            </Link>
            <Link to="/signup" className="hover:text-dark transition-colors">
              Sign up
            </Link>
            <a href="#" className="hover:text-dark transition-colors">
              GitHub
            </a>
          </div>

          <div>&copy; 2026 CodeArena</div>
        </div>
      </footer>

      {/* 10. FIXED FLOATING NAV */}
      <div className="fixed bottom-6 left-1/2 transform -translate-x-1/2 z-50">
        <div className="bg-white/90 backdrop-blur-md border border-slate-200 shadow-lg rounded-full p-1.5 flex items-center gap-4 pr-1.5 pl-4">
          <span className="font-accent text-xl font-bold text-primary-dark">
            C
          </span>
          <div className="w-px h-4 bg-slate-200"></div>
          <LandingButton
            href="/signup"
            variant="primary"
            className="!py-2 !px-5 !text-xs">
            
            Start solving
          </LandingButton>
        </div>
      </div>
    </div>);

}
// --- Helper Components for Landing ---
function MarqueeCard({
  title,
  difficulty,
  status,
  lang





}: {title: string;difficulty: string;status: string;lang: string;}) {
  const isAccepted = status === 'Accepted';
  const isWA = status === 'Wrong Answer';
  return (
    <div className="w-[300px] flex-shrink-0 bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
      <div className="flex justify-between items-start mb-4">
        <h4 className="font-medium text-dark truncate pr-2">{title}</h4>
        <span
          className={`text-[10px] font-medium px-2 py-0.5 rounded border ${difficulty === 'Easy' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : difficulty === 'Medium' ? 'bg-amber-50 text-amber-700 border-amber-200' : 'bg-red-50 text-red-700 border-red-200'}`}>
          
          {difficulty}
        </span>
      </div>

      <div className="bg-slate-50 rounded p-3 mb-4 font-mono text-[10px] text-muted-dark border border-slate-100">
        <div className="flex justify-between mb-1">
          <span className="text-slate-400">{lang}</span>
        </div>
        <div className="truncate">class Solution:</div>
        <div className="truncate pl-2">def solve(self):</div>
        <div className="truncate pl-4 text-slate-400"># ...</div>
      </div>

      <div className="flex items-center gap-1.5 text-xs font-medium">
        {isAccepted ?
        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" /> :
        isWA ?
        <XCircle className="w-3.5 h-3.5 text-red-500" /> :

        <Clock className="w-3.5 h-3.5 text-amber-500" />
        }
        <span
          className={
          isAccepted ?
          'text-emerald-700' :
          isWA ?
          'text-red-700' :
          'text-amber-700'
          }>
          
          {status}
        </span>
      </div>
    </div>);

}
function FeatureCard({
  icon,
  title,
  description,
  delay





}: {icon: React.ReactNode;title: string;description: string;delay: number;}) {
  return (
    <AnimatedSection
      delay={delay}
      className="p-6 rounded-2xl border border-slate-100 bg-slate-50/50 hover:bg-slate-50 transition-colors">
      
      <div className="w-10 h-10 rounded-lg bg-white border border-slate-200 flex items-center justify-center text-primary-dark mb-5 shadow-sm">
        {icon}
      </div>
      <h3 className="text-lg font-medium text-dark mb-2">{title}</h3>
      <p className="text-sm text-muted-dark leading-relaxed">{description}</p>
    </AnimatedSection>);

}
function TestimonialAvatar({
  name,
  role,
  delay




}: {name: string;role: string;delay: number;}) {
  return (
    <AnimatedSection delay={delay} className="flex items-center gap-3">
      <div className="w-10 h-10 rounded-full bg-slate-200 border-2 border-white shadow-sm flex items-center justify-center text-muted-dark font-medium text-sm">
        {name.charAt(0)}
      </div>
      <div className="text-left">
        <div className="text-sm font-medium text-dark">{name}</div>
        <div className="text-xs text-muted-dark">{role}</div>
      </div>
    </AnimatedSection>);

}
function UseCaseCard({ title, delay }: {title: string;delay: number;}) {
  return (
    <AnimatedSection
      delay={delay}
      className="p-6 rounded-xl border border-slate-200 bg-white text-center hover:border-primary-dark transition-colors cursor-default">
      
      <h3 className="font-medium text-dark">{title}</h3>
    </AnimatedSection>);

}